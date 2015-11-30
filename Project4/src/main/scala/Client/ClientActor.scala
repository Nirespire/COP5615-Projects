package Client

import java.security.KeyPairGenerator
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

import Client.ClientType.ClientType
import Client.Messages._
import Client.Resources._
import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.PostType._
import Objects._
import Utils.Constants
import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._
import spray.json._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}


class ClientActor(isPage: Boolean = false, clientType: ClientType) extends Actor with ActorLogging {
  val myPages = mutable.ArrayBuffer[Page]()
  val myFriends = mutable.ArrayBuffer[Int]()
  val myRealFriends = mutable.HashMap[ActorRef, Int]()
  val waitForIdFriends = mutable.Set[ActorRef]()
  val returnHandshake = mutable.Set[ActorRef]()
  var me: User = null
  var mePage: Page = null
  var myBaseObj: BaseObject = null
  var numPosts = 0
  var numAlbums = 0
  var numPictures = 0

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
    case false if !myBaseObj.deleted =>
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
    /*TODO
    case UpdatePicture(pictureID) =>

    case UpdateAlbum(albumID) =>

    case UpdateSelf =>

    case UpdatePost(postType, attachmentID) =>

     */

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

    /*TODO check this*/
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
    case PutMsg(response, reaction) =>
      reaction match {
        case "user" =>
          me = response ~> unmarshal[User]
          myBaseObj = me.baseObject
          ProfileMap.obj.put(myBaseObj.id, isPage)
          waitForIdFriends.foreach(f => self ! f)
          waitForIdFriends.clear()
          returnHandshake.foreach(f => self.tell(Handshake(Constants.trueBool, myBaseObj.id), f))
          returnHandshake.clear()
          //          log.info(s"Printing $me - $myBaseObj")
          self ! Constants.falseBool
          if (myBaseObj.id == 0) get("debug")
        case "page" =>
          mePage = response ~> unmarshal[Page]
          myBaseObj = mePage.baseObject
          ProfileMap.obj.put(myBaseObj.id, isPage)
          waitForIdFriends.foreach(f => self ! f)
          waitForIdFriends.clear()
          returnHandshake.foreach(f => self.tell(Handshake(Constants.trueBool, myBaseObj.id), f))
          returnHandshake.clear()
          //          log.info(s"Printing $mePage - $myBaseObj")
          self ! Constants.falseBool
          if (myBaseObj.id == 0) get("debug")
        case "post" =>
          numPosts += 1
        //          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePost)
        case "album" =>
          numAlbums += 1
        //          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeAlbum)
        case "picturePost" =>
          numPictures += 1
          val picture = response ~> unmarshal[Picture]
          context.system.scheduler.scheduleOnce(durationSeconds(1), self, MakePost(photo, picture.baseObject.id))
        case "picture" =>
          numPictures += 1
        //          context.system.scheduler.scheduleOnce(randomDuration(5), self, MakePicture)
        case "likepage" =>
        case "like" =>
      }
    case GetMsg(response, reaction) =>
      reaction match {
        case "postdelete" => delete(response.entity.asString.parseJson.asJsObject, "post")
        case "albumdelete" => delete(response.entity.asString.parseJson.asJsObject, "album")
        case "picturedelete" => delete(response.entity.asString.parseJson.asJsObject, "picture")
        case "debug" =>
          log.info(s"${response.entity.asString}")
          context.system.scheduler.scheduleOnce(durationSeconds(2), self, DebugMsg)
        case "user" =>
        case "page" =>
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
          try {
            val post = response ~> unmarshal[Post]
            val id = post.baseObject.id
            if (random(2) == 0 && id > 0) get(s"post/${post.creator}/${id - 1}", "post")
            if (random(2) == 0) putRoute(s"like/${post.from}/$reaction/${post.baseObject.id}/${myBaseObj.id}", "like")
          } catch {
            case e: Throwable => log.info(s"$response")
          }
        case "getalbumaddpicture" =>
          val album = response ~> unmarshal[Album]
          context.system.scheduler.scheduleOnce(randomDuration(5), self, MakePicture(album.baseObject.id))
        case "getfriendpost" =>
        //            context.system.scheduler.scheduleOnce(randomDuration(5), self, GetFriendsPost)
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
    case PostMsg(response, reaction) =>
      try {
        reaction match {
          case "addfriend" =>
            val updFL = response ~> unmarshal[UpdateFriendList]
            myFriends.append(updFL.fid)
          //          log.info("added friend {}", updFL)
          //          context.system.scheduler.scheduleOnce(randomDuration(5), self, MakeFriend)
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

  def registerMyself() {
    if (isPage) {
      val newPage = Page(BaseObject(), "about", pageCategories(Random.nextInt(pageCategories.length)), -1)
      put(newPage.toJson.asJsObject, "page")
    } else {
      val fullName = Resources.names(Random.nextInt(Resources.names.length)).split(' ')
      val gender = Random.nextInt(2)
      val newUser = User(BaseObject(), "about me", Resources.randomBirthday(), if (gender == 0) 'M' else 'F', fullName(1), fullName(0))
      put(newUser.toJson.asJsObject, "user")
    }
  }


  def putRoute(route: String, inputReaction: String = ""): Unit = {
    val reaction = if (inputReaction.nonEmpty) inputReaction else route
    val pipeline = sendReceive
    val future = pipeline {
      pipelining.Put(s"http://$serviceHost:$servicePort/$route")
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
      pipelining.Put(s"http://$serviceHost:$servicePort/$route", json)
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
      pipelining.Get(s"http://$serviceHost:$servicePort/$route")
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
      pipelining.Post(s"http://$serviceHost:$servicePort/$route", json)
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
      pipelining.Delete(s"http://$serviceHost:$servicePort/$route", json)
    }

    future onComplete {
      case Success(response) => self ! DeleteMsg(response, reaction)
      case Failure(error) => log.error(error, s"Couldn't post $json using $route")
    }
  }
}