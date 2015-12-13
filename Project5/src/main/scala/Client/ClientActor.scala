package Client

import java.security.PublicKey

import Client.ClientType.ClientType
import Client.Messages._
import Objects.ObjectTypes.ObjectType
import Objects.ObjectTypes.ObjectType
import Objects.ObjectTypes.ObjectType.ObjectType
import Utils.Resources._
import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.PostType._
import Objects._
import Server.Messages.ResponseMessage
import Utils.{Resources, Base64Util, Crypto, Constants}
import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._
import spray.http.HttpResponse
import spray.http.StatusCodes._
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
    case false if !myBaseObj.deleted => activity()
    case MakePost(postType, attachmentID) =>
      val newPost = Objects.Post(new DateTime().toString(), statuses(Random.nextInt(statuses.length)), postType, attachmentID)
      val keys = Map(myBaseObj.id.toString -> keyPair.getPublic)
      put(createSecureObjectMessage(newPost, myBaseObj.id, myBaseObj.id, ObjectType.post, keys), "post")
    case MakePicturePost =>
      val newPicture = Picture("filename", "")
      val keys = Map(myBaseObj.id.toString -> keyPair.getPublic)
      put(createSecureObjectMessage(newPicture, myBaseObj.id, myBaseObj.id, ObjectType.picture, keys), "picturepost")
    case MakePicture(albumID) =>
      val newPicture = Picture("filename", "")
      val keys = Map(myBaseObj.id.toString -> keyPair.getPublic)
      put(createSecureObjectMessage(newPicture, myBaseObj.id, myBaseObj.id, ObjectType.picture, keys), "picture")
    case AddPictureToAlbum =>
    case MakeAlbum =>
      val newAlbum = Album(new DateTime().toString, new DateTime().toString, -1, "album desc")
      val keys = Map(myBaseObj.id.toString -> keyPair.getPublic)
      put(createSecureObjectMessage(newAlbum, myBaseObj.id, myBaseObj.id, ObjectType.album, keys), "album")
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
        if (needResponse) {
          self ! MakeFriend(id)
          sender ! Handshake(Constants.falseBool, myBaseObj.id)
        }
      }
    case MakeFriend(id) =>
    // TODO Post("/addFriend")
    // SecureMessage(SecureRequest(friendID))

    case PutMsg(response, reaction) => handlePutResponse(response, reaction)
    case GetMsg(response, reaction) => handleGetResponse(response, reaction)
    case PostMsg(response, reaction) =>
      try {
        reaction match {
          case "addfriend" =>
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
    val pipeline = sendReceive

    val future = pipeline {
      pipelining.Get(s"http://$serviceHost:$servicePort/server_key")
    }

    future onComplete {
      case Success(response) =>
        val returnBytes = response ~> unmarshal[Array[Byte]]
        serverPublicKey = Crypto.constructRSAPublicKeyFromBytes(returnBytes)

        val future2 = pipeline {
          pipelining.Put(s"http://$serviceHost:$servicePort/register", keyPair.getPublic.getEncoded)
        }

        future2 onComplete {
          case Success(response) =>
            val secureMsg = response ~> unmarshal[SecureMessage]
            val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, keyPair.getPrivate)
            if (Crypto.verifySign(serverPublicKey, secureMsg.signature, requestKeyBytes)) {
              val requestKey = Crypto.constructAESKeyFromBytes(requestKeyBytes)
              val requestJson = Crypto.decryptAES(secureMsg.message, requestKey, Constants.IV)
              myBaseObj = BaseObject(id = Base64Util.decodeString(requestJson).toInt)


              if (isPage) {
                mePage = Page("about", Resources.getRandomPageCategory(), -1, keyPair.getPublic.getEncoded)
                val secureObject = Crypto.constructSecureObject(myBaseObj, myBaseObj.id, myBaseObj.id, ObjectType.page.id, mePage.toJson.compactPrint, Map(myBaseObj.id.toString -> keyPair.getPublic))
                val secureMessage = Crypto.constructSecureMessage(myBaseObj.id, secureObject.toJson.compactPrint, serverPublicKey, keyPair.getPrivate)
                val future3 = pipeline {
                  pipelining.Put(s"http://$serviceHost:$servicePort/page", secureMessage)
                }

                future3 onComplete {
                  case Success(response) =>
                    self ! false
                  case Failure(error) => log.error(error, s"Couldn't put Page")
                }
              }

              else {
                val fullName = Resources.names(Random.nextInt(Resources.names.length)).split(' ')
                me = User("about", Resources.randomBirthday(), 'M', fullName(0), fullName(1), keyPair.getPublic.getEncoded)
                val secureObject = Crypto.constructSecureObject(myBaseObj, myBaseObj.id, myBaseObj.id, ObjectType.user.id, me.toJson.compactPrint, Map(myBaseObj.id.toString -> keyPair.getPublic))
                val secureMessage = Crypto.constructSecureMessage(myBaseObj.id, secureObject.toJson.compactPrint, serverPublicKey, keyPair.getPrivate)
                val future3 = pipeline {
                  pipelining.Put(s"http://$serviceHost:$servicePort/user", secureMessage)
                }

                future3 onComplete {
                  case Success(response) =>
                    self ! false
                  case Failure(error) => log.error(error, s"Couldn't put User")
                }
              }
            }
          case Failure(error) => log.error(error, s"Couldn't register")
        }
      case Failure(error) => log.error(error, s"Couldn't get server_key")
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

  def put(json: SecureMessage, route: String, inputReaction: String = ""): Unit = {
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

  def post(json: SecureMessage, route: String, inputReaction: String = "") = {
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

  def delete(json: SecureMessage, route: String, inputReaction: String = "") = {
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

  def activity() = {
    //    log.info(myBaseObj.id + " starting activity")
    //    if (isPage) get(s"page/${myBaseObj.id}", "page") else get(s"user/${myBaseObj.id}", "user")

    //    if (random(1001) <= 5) {
    //      random(3) match {
    //        case 0 => if (numPosts > 0) get(s"post/${myBaseObj.id}/${random(numPosts) + 2}", "postdelete")
    //        case 1 => if (numAlbums > 0) get(s"album/${myBaseObj.id}/${random(numAlbums) + 2}", "albumdelete")
    //        case 2 => if (numPictures > 0) get(s"picture/${myBaseObj.id}/${random(numPictures) + 2}", "picturedelete")
    //      }
    //    }

    if (random(101) < putPercent) {
      random(4) match {
        case 0 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePost(status, -1))
        case 1 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakeAlbum)
        case 2 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePicture(-1))
        case 3 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePicturePost)
      }
    }

    //    if (random(101) < getPercent) {
    //      myRealFriends.foreach { case (ref: ActorRef, id: Int) =>
    //        if (ProfileMap.obj(id)) {
    //          get(s"page/$id", "page")
    //        } else {
    //          get(s"user/$id", "user")
    //          if (!isPage && random(2) == 0) get(s"friendlist/$id/0", "friendlist")
    //        }
    //        if (random(2) == 0) get(s"feed/$id", "feed") else get(s"post/$id", "post")
    //        if (random(2) == 0) get(s"album/$id", "album") else get(s"picture/$id", "picture")
    //      }
    //    }

    //    if (random(101) < friendPercent) {
    //      context.system.scheduler.scheduleOnce(randomDuration(3), self, MakeFriend)
    //    }

    //    if (random(101) < updatePercent) {
    //      context.system.scheduler.scheduleOnce(randomDuration(3), self, UpdatePost(status, -1))
    //      if (numAlbums > 0) {
    //        context.system.scheduler.scheduleOnce(randomDuration(3), self, AddPictureToAlbum)
    //      }
    //    }

    context.system.scheduler.scheduleOnce(randomDuration(3), self, Constants.falseBool)
    // Delete case
    if (random(100001) < 5) {
      //      if (isPage)
      //      else
    }
  }

  def handlePutResponse(response: HttpResponse, reaction: String) = {
    val updateRequest = random(101) < updatePercent

    reaction match {
      case "registerUser" =>

      case "registerPage" =>


      case "user" | "page" =>
        //        if (reaction == "user") {
        //          me = response ~> unmarshal[User]
        //          myBaseObj = me.baseObject
        //
        //        } else {
        //          mePage = response ~> unmarshal[Page]
        //          myBaseObj = mePage.baseObject
        //        }

        ProfileMap.obj.put(myBaseObj.id, isPage)
        waitForIdFriends.foreach(f => self ! f)
        waitForIdFriends.clear()
        returnHandshake.foreach(f => self.tell(Handshake(Constants.trueBool, myBaseObj.id), f))
        returnHandshake.clear()
        //          log.info(s"Printing $me - $myBaseObj")
        self ! Constants.falseBool
        if (myBaseObj.id == 0) get("debug")
        if (updateRequest) post(response.entity.asString.parseJson.asInstanceOf[SecureMessage], "profile")
      case "post" =>
        numPosts += 1
      case "album" =>
        numAlbums += 1
      case "picturepost" =>
        numPictures += 1
      case "picture" =>
        numPictures += 1
      case "likepage" =>
      case "like" =>
    }
  }

  def handleGetResponse(response: HttpResponse, reaction: String) = {
    reaction match {
      case "postdelete" => delete(response.entity.asString.parseJson.asInstanceOf[SecureMessage], "post")
      case "albumdelete" => delete(response.entity.asString.parseJson.asInstanceOf[SecureMessage], "album")
      case "picturedelete" => delete(response.entity.asString.parseJson.asInstanceOf[SecureMessage], "picture")
      case "debug" =>
        log.info(s"${response.entity.asString}")
        context.system.scheduler.scheduleOnce(durationSeconds(2), self, DebugMsg)
      case "user" | "page" =>
      case "friendlist" =>
      case "feed" =>
      case "feedpost" =>
      case "picture" =>
      case "post" =>
      case "getalbumaddpicture" =>
      case "album" =>
      case x => log.error("Unmatched getmsg case {}", x)
    }
  }

  def createSecureObjectMessage(obj: Any, from: Int, to: Int, objType: ObjectType, keys: Map[String, PublicKey]): SecureMessage = {
    objType match {
      case ObjectType.post =>
        val secureObject = Crypto.constructSecureObject(new BaseObject(), from, to, ObjectType.post.id, obj.asInstanceOf[Post].toJson.compactPrint, keys)
        Crypto.constructSecureMessage(myBaseObj.id, secureObject.toJson.compactPrint, serverPublicKey, keyPair.getPrivate)
      case ObjectType.picture =>
        val secureObject = Crypto.constructSecureObject(new BaseObject(), from, to, ObjectType.picture.id, obj.asInstanceOf[Picture].toJson.compactPrint, keys)
        Crypto.constructSecureMessage(myBaseObj.id, secureObject.toJson.compactPrint, serverPublicKey, keyPair.getPrivate)
      case ObjectType.album =>
        val secureObject = Crypto.constructSecureObject(new BaseObject(), from, to, ObjectType.album.id, obj.asInstanceOf[Album].toJson.compactPrint, keys)
        Crypto.constructSecureMessage(myBaseObj.id, secureObject.toJson.compactPrint, serverPublicKey, keyPair.getPrivate)
    }
  }
}