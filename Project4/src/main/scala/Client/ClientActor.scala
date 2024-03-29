package Client

import java.security.PublicKey

import Client.ClientType.ClientType
import Client.Messages._
import Client.Resources._
import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.PostType._
import Objects._
import Server.Messages.ResponseMessage
import Utils.{Base64Util, Crypto, Constants}
import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._
import spray.http.HttpResponse
import spray.json._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}


class ClientActor(isPage: Boolean = false, clientType: ClientType) extends Actor with ActorLogging {
  val myPages = mutable.ArrayBuffer[Page]()
  val myFriends = mutable.ArrayBuffer[Int]()
  val myRealFriends = mutable.HashMap[ActorRef, Int]()
  val myFriendPublicKeys = mutable.HashMap[String, Int]()
  val waitForIdFriends = mutable.Set[ActorRef]()
  val returnHandshake = mutable.Set[ActorRef]()
  var me: User = null
  var mePage: Page = null
  var myBaseObj: BaseObject = null
  var numPosts = 0
  var numAlbums = 0
  var numPictures = 0

  private val keyPair = Crypto.generateRSAKeys()
  private var authToken:String = null
  var serverPublicKey: PublicKey = null

  val (putPercent, getPercent, friendPercent, updatePercent) = clientType match {
    case ClientType.Active => (80, 50, 90, 50)
    case ClientType.Passive => (20, 90, 80, 5)
    case ClientType.ContentCreator => (70, 20, 10, 40)
  }

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  import context.dispatcher

  def random(n: Int) = Random.nextInt(n)

  def durationSeconds(n: Int) = n.seconds

  def randomDuration(n: Int) = durationSeconds(random(n))

  def receive = {
    // Create a user profile or page for self
    case true => registerMyself()
    // Do some activity every second
    case false if !myBaseObj.deleted => activity()
    case MakePost(postType, attachmentID) =>
      val newPost = Objects.Post(baseObject = BaseObject(), myBaseObj.id, new DateTime().toString(), myBaseObj.id, statuses(Random.nextInt(statuses.length)), postType, attachmentID)
      put(newPost.toJson.asJsObject, "post")
    case MakePicturePost =>
      val newPicture = Picture(BaseObject(), myBaseObj.id, -1, "filename.png", "blah")
      put(newPicture.toJson.asJsObject, "picture", "picturePost")
    case MakePicture(albumID) =>
      val newPicture = Picture(BaseObject(), myBaseObj.id, albumID, "filename.png", "blah")
      put(newPicture.toJson.asJsObject, "picture")
    case AddPictureToAlbum =>
      get(s"album/${myBaseObj.id}", "getalbumaddpicture")
    case MakeAlbum =>
      val newAlbum = Album(baseObject = BaseObject(), myBaseObj.id, new DateTime().toString(), new DateTime().toString(), -1, "My new Album")
      put(newAlbum.toJson.asJsObject, "album")
    // From matchmaker
    case aNewFriend: ActorRef =>
      if (myBaseObj == null) {
        waitForIdFriends.add(aNewFriend)
      } else {
        //        log.info(myBaseObj.id + " just met someone")
        aNewFriend ! Handshake(Constants.trueBool, myBaseObj.id)
      }
    // From new friend
    case Handshake(needResponse, id) =>
      if (myBaseObj == null) {
        returnHandshake.add(sender)
      } else {
        myRealFriends.put(sender, id)
        self ! MakeFriend(id)
        if (needResponse) {
          sender ! Handshake(Constants.falseBool, myBaseObj.id)
        }
      }

    case MakeFriend(id) =>
      if (isPage) {
        if (id != -1) {
          putRoute(s"like/${myBaseObj.id}/page/-1/$id", "likepage")
        }
      } else {
        val friendId = if (id == -1) Random.nextInt(myBaseObj.id) else id
        if (ProfileMap.obj(friendId)) {
          putRoute(s"like/$friendId/page/-1/${myBaseObj.id}", "likepage")
        } else {
          val updatedList = UpdateFriendList(myBaseObj.id, friendId)
          post(updatedList.toJson.asJsObject, "addfriend")
        }
      }
    case PutMsg(response, reaction) => handlePutResponse(response, reaction)
    case GetMsg(response, reaction) => handleGetResponse(response, reaction)
    case PostMsg(response, reaction) =>
      try {
        reaction match {
          case "addfriend" =>
            val updFL = response ~> unmarshal[UpdateFriendList]
            myFriends.append(updFL.fid)
          //          log.info("added friend {}", updFL)
          //          context.system.scheduler.scheduleOnce(randomDuration(5), self, MakeFriend)
          case _ => //log.info(s"Updated $reaction")
        }
      } catch {
        case e: Throwable => log.error(e, "Error for response {}", response)
      }
    case DebugMsg => get("debug")

    case DeleteMsg(response, reaction) =>
      reaction match {
        case "user" | "page" => myBaseObj.deleted = true
        case _ => log.info(s"${myBaseObj.id} - ${response.entity.asString}")
      }
  }

  def registerMyself() = {
    if (isPage) {
      val newPage = Page(BaseObject(), "about", pageCategories(Random.nextInt(pageCategories.length)), -1, Base64Util.encodeString(keyPair.getPublic().getEncoded()))
      //      put(newPage.toJson.asJsObject, "page")
      put(newPage.toJson.asJsObject, "registerPage")
    } else {
      val fullName = Resources.names(Random.nextInt(Resources.names.length)).split(' ')
      val gender = Random.nextInt(2)
      val newUser = User(BaseObject(), "about me", Resources.randomBirthday(), if (gender == 0) 'M' else 'F', fullName(1), fullName(0), Base64Util.encodeString(keyPair.getPublic().getEncoded()))
//      put(newUser.toJson.asJsObject, "user")
      put(newUser.toJson.asJsObject, "registerUser")
    }
  }


  def putRoute(route: String, inputReaction: String = ""): Unit = {
    val reaction = if (inputReaction.nonEmpty) inputReaction else route
    val pipeline = sendReceive
    val future = pipeline {
      pipelining.Put(s"http://$serviceHost:$servicePort/$route") ~> addHeader("AuthToken", if(authToken == null)"" else authToken)
    }

    future onComplete {
      case Success(response) => self ! PutMsg(response, reaction)
      case Failure(error) => log.error(error, s"Couldn't create using $route")
    }
  }

  def put(json: JsObject, route: String, inputReaction: String = ""): Unit = {
    val reaction = if (inputReaction.nonEmpty) inputReaction else route
    val pipeline = sendReceive
    val future = pipeline {
      pipelining.Put(s"http://$serviceHost:$servicePort/$route", json) ~> addHeader(Constants.authTokenHeader, if(authToken == null)"" else authToken)
    }

    future onComplete {
      case Success(response) => self ! PutMsg(response, reaction)
      case Failure(error) => log.error(error, s"Couldn't create $json using $route")
    }
  }

  def putWithHeader(json: JsObject, route: String, inputReaction: String = "", header: Tuple2[String,String]): Unit = {
    val reaction = if (inputReaction.nonEmpty) inputReaction else route
    val pipeline = sendReceive
    val future = pipeline {
      pipelining.Put(s"http://$serviceHost:$servicePort/$route", json) ~> addHeader(header._1, header._2)
    }

    future onComplete {
      case Success(response) => self ! PutMsg(response, reaction)
      case Failure(error) => log.error(error, s"Couldn't create $json using $route")
    }
  }

  def get(route: String, inputReaction: String = "") = {
    val reaction = if (inputReaction.nonEmpty) inputReaction else route
    val pipeline = sendReceive
    val future = pipeline {
      pipelining.Get(s"http://$serviceHost:$servicePort/$route") ~> addHeader(Constants.authTokenHeader, if(authToken == null)"" else authToken)
    }

    future onComplete {
      case Success(response) => self ! GetMsg(response, reaction)
      case Failure(error) => log.error(error, s"Couldn't get $route")
    }
  }

  def post(json: JsObject, route: String, inputReaction: String = "") = {
    val reaction = if (inputReaction.nonEmpty) inputReaction else route
    val pipeline = sendReceive
    val future = pipeline {
      pipelining.Post(s"http://$serviceHost:$servicePort/$route", json) ~> addHeader(Constants.authTokenHeader, if(authToken == null)"" else authToken)
    }

    future onComplete {
      case Success(response) => self ! PostMsg(response, reaction)
      case Failure(error) => log.error(error, s"Couldn't post $json using $route")
    }
  }

  def delete(json: JsObject, route: String, inputReaction: String = "") = {
    val reaction = if (inputReaction.nonEmpty) inputReaction else route
    val pipeline = sendReceive
    val future = pipeline {
      pipelining.Delete(s"http://$serviceHost:$servicePort/$route", json) ~> addHeader(Constants.authTokenHeader, if(authToken == null)"" else authToken)
    }

    future onComplete {
      case Success(response) => self ! DeleteMsg(response, reaction)
      case Failure(error) => log.error(error, s"Couldn't post $json using $route")
    }
  }

  def activity() = {
    //      log.info(myBaseObj.id + " starting activity")
    if (isPage) get(s"page/${myBaseObj.id}", "page") else get(s"user/${myBaseObj.id}", "user")

    if (random(1001) <= 5) {
      random(3) match {
        case 0 => if (numPosts > 0) get(s"post/${myBaseObj.id}/${random(numPosts) + 2}", "postdelete")
        case 1 => if (numAlbums > 0) get(s"album/${myBaseObj.id}/${random(numAlbums) + 2}", "albumdelete")
        case 2 => if (numPictures > 0) get(s"picture/${myBaseObj.id}/${random(numPictures) + 2}", "picturedelete")
      }
    }

    if (random(101) < putPercent) {
      random(4) match {
        case 0 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePost(status, -1))
        case 1 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakeAlbum)
        case 2 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePicture(-1))
        case 3 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePicturePost)
      }
    }

    if (random(101) < getPercent) {
      myRealFriends.foreach { case (ref: ActorRef, id: Int) =>
        if (ProfileMap.obj(id)) {
          get(s"page/$id", "page")
        } else {
          get(s"user/$id", "user")
          if (!isPage && random(2) == 0) get(s"friendlist/$id/0", "friendlist")
        }
        if (random(2) == 0) get(s"feed/$id", "feed") else get(s"post/$id", "post")
        if (random(2) == 0) get(s"album/$id", "album") else get(s"picture/$id", "picture")
      }
    }

    if (random(101) < friendPercent) {
      context.system.scheduler.scheduleOnce(randomDuration(3), self, MakeFriend)
    }

    if (random(101) < updatePercent) {
      context.system.scheduler.scheduleOnce(randomDuration(3), self, UpdatePost(status, -1))
      if (numAlbums > 0) {
        context.system.scheduler.scheduleOnce(randomDuration(3), self, AddPictureToAlbum)
      }
    }

    context.system.scheduler.scheduleOnce(randomDuration(3), self, Constants.falseBool)
    if (random(100001) < 5) {
      if (isPage) delete(mePage.toJson.asJsObject, "page")
      else delete(me.toJson.asJsObject, "user")
    }
  }

  def handlePutResponse(response: HttpResponse, reaction: String) = {
    val updateRequest = random(101) < updatePercent

    reaction match {
      case "registerUser" =>
//        log.info("got register response")
        me = response ~> unmarshal[User]
        myBaseObj = me.baseObject

        // TODO better way to get RandomString out of the headers?
        var randomString:String = null
        response.headers.foreach{i =>
          if(i.name == Constants.randomStringHeader){
            randomString = i.value
          }
          else if(i.name == Constants.serverPublicKeyHeader){
            serverPublicKey = Crypto.constructRSAPublicKeyFromBytes(Base64Util.decodeBinary(i.value))
          }
        }
//        log.info(randomString)

        val signedBytes = Crypto.signData(keyPair.getPrivate(), Base64Util.encodeBinary(randomString))

        putWithHeader(me.toJson.asJsObject, "user", "user", (Constants.signedStringHeader, Base64Util.encodeString(signedBytes)))

      case "registerPage" =>
        mePage = response ~> unmarshal[Page]
        myBaseObj = mePage.baseObject

        // TODO better way to get RandomString out of the headers?
        var randomString:String = null
        response.headers.foreach{i =>
          if(i.name == Constants.randomStringHeader){
            randomString = i.value
          }
          else if(i.name == Constants.serverPublicKeyHeader){
            serverPublicKey = Crypto.constructRSAPublicKeyFromBytes(Base64Util.decodeBinary(i.value))
          }
        }
        //        log.info(randomString)

        val signedBytes = Crypto.signData(keyPair.getPrivate(), Base64Util.encodeBinary(randomString))

        putWithHeader(mePage.toJson.asJsObject, "page", "page", (Constants.signedStringHeader, Base64Util.encodeString(signedBytes)))

      case "user" | "page" =>
        if (reaction == "user") {
          me = response ~> unmarshal[User]
          myBaseObj = me.baseObject

        } else {
          mePage = response ~> unmarshal[Page]
          myBaseObj = mePage.baseObject
        }
        // TODO better way to get AuthToken out of the headers?
        response.headers.foreach{i =>
          if(i.name == Constants.authTokenHeader){
            authToken = i.value
            //              log.info(authToken)
          }
        }

        ProfileMap.obj.put(myBaseObj.id, isPage)
        waitForIdFriends.foreach(f => self ! f)
        waitForIdFriends.clear()
        returnHandshake.foreach(f => self.tell(Handshake(Constants.trueBool, myBaseObj.id), f))
        returnHandshake.clear()
        //          log.info(s"Printing $me - $myBaseObj")
        self ! Constants.falseBool
        if (myBaseObj.id == 0) get("debug")
        if (updateRequest) post(response.entity.asString.parseJson.asJsObject, "profile")
      case "post" =>
        numPosts += 1
        if (updateRequest) post(response.entity.asString.parseJson.asJsObject, "post")
      case "album" =>
        numAlbums += 1
        if (updateRequest) post(response.entity.asString.parseJson.asJsObject, "album")
      case "picturePost" =>
        numPictures += 1
        val picture = response ~> unmarshal[Picture]
        if (updateRequest) post(response.entity.asString.parseJson.asJsObject, "picture")
      case "picture" =>
        numPictures += 1
        if (updateRequest) post(response.entity.asString.parseJson.asJsObject, "picture")
      case "likepage" =>
      case "like" =>
    }
  }

  def handleGetResponse(response: HttpResponse, reaction: String) = {
    reaction match {
      case "postdelete" => delete(response.entity.asString.parseJson.asJsObject, "post")
      case "albumdelete" => delete(response.entity.asString.parseJson.asJsObject, "album")
      case "picturedelete" => delete(response.entity.asString.parseJson.asJsObject, "picture")
      case "debug" =>
        log.info(s"${response.entity.asString}")
        context.system.scheduler.scheduleOnce(durationSeconds(2), self, DebugMsg)
      case "user" | "page" =>
      case "friendlist" =>
        val arr = response ~> unmarshal[Array[Int]]
        if (arr.nonEmpty) {
          val makeFriendIdx = random(arr.length)
          if (!myFriends.contains(arr(makeFriendIdx))) {
            post(UpdateFriendList(myBaseObj.id, arr(makeFriendIdx)).toJson.asJsObject(), "addfriend")
          }
        }
      case "feed" =>
        val arr = response ~> unmarshal[Array[Int]]
        (1 until arr.length).foreach(pIdx => get(s"post/${arr(0)}/${arr(pIdx)}", "feedpost"))
      case "feedpost" =>
        val post = response ~> unmarshal[Post]
        if (random(2) == 0) putRoute(s"like/${post.from}/$reaction/${post.baseObject.id}/${myBaseObj.id}", "like")
      case "picture" =>
        val picture = response ~> unmarshal[Picture]
        if (random(2) == 0) putRoute(s"like/${picture.from}/$reaction/${picture.baseObject.id}/${myBaseObj.id}", "like")
      case "post" =>
        val post = response ~> unmarshal[Post]
        val id = post.baseObject.id
        if (random(2) == 0 && id > 0) get(s"post/${post.creator}/${id - 1}", "post")
        if (random(2) == 0) putRoute(s"like/${post.from}/$reaction/${post.baseObject.id}/${myBaseObj.id}", "like")
      case "getalbumaddpicture" =>
        val album = response ~> unmarshal[Album]
        context.system.scheduler.scheduleOnce(randomDuration(5), self, MakePicture(album.baseObject.id))
      case "album" =>
        val album = response ~> unmarshal[Album]
        if (random(2) == 0) putRoute(s"like/${album.from}/$reaction/${album.baseObject.id}/${myBaseObj.id}", "like")
        if (album.pictures.nonEmpty) {
          (0 until random(album.pictures.size)).foreach { pIdx =>
            get(s"picture/${album.from}/$pIdx", "picture")
          }
        }
      case x => log.error("Unmatched getmsg case {}", x)
    }
  }
}