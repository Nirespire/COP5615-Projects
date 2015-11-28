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
import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._
import spray.http.{HttpResponse, HttpEntity}
import spray.json._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}


class ClientActor(isPage: Boolean = false, clientType: ClientType) extends Actor with ActorLogging {

  var myFriends = mutable.ArrayBuffer[Int]()

  var myRealFriends = mutable.HashMap[ActorRef, Int]()

  var me: User = null
  var mePage: Page = null
  var myBaseObj: BaseObject = null

  var numPosts = 0
  var numAlbums = 0

  val (putPercent, getPercent, friendPercent, updatePercent) = clientType match {
    case ClientType.Active => (80, 50, 90, 50)
    case ClientType.Passive => (20, 90, 80, 5)
    case ClientType.ContentCreator => (70, 20, 10, 40)
  }

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  val myPages = mutable.ArrayBuffer[Page]()

  import context.dispatcher

  def receive = {
    // Create a user profile or page for self
    case true =>
      registerMyself()

    // Do some activity every second
    case false =>
      if (myBaseObj == null) {
        context.system.scheduler.scheduleOnce(1 second, self, false)
      }
      else {
        //        log.info(myBaseObj.id + " starting activity")
        val probability = Random.nextInt(101)

        if (isPage) {
          getOrDeleteObject("Page", myBaseObj.id, true)
        }
        else {
          getOrDeleteObject("User", myBaseObj.id, true)
        }

        if (probability < putPercent) {

          val rand = Random.nextInt(4)

          rand match {
            case 0 =>
              context.system.scheduler.scheduleOnce(Random.nextInt(3) second, self, MakePost(status, -1))

            case 1 =>
              context.system.scheduler.scheduleOnce(Random.nextInt(3) second, self, MakeAlbum)

            case 2 =>
              context.system.scheduler.scheduleOnce(Random.nextInt(3) second, self, MakePicture(-1))

            case 3 =>
              context.system.scheduler.scheduleOnce(Random.nextInt(3) second, self, MakePicturePost)
            case _ =>
          }
        }

        if (probability < getPercent) {
          if (numPosts > 50) {
            context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, GetFriendsPost)
          }
        }

        if (probability < friendPercent) {
          //context.system.scheduler.scheduleOnce(Random.nextInt(3) second, self, MakeFriend)
        }

        if (probability < updatePercent) {
          context.system.scheduler.scheduleOnce(Random.nextInt(3) second, self, UpdatePost(status, -1))

          if (numAlbums > 0) {
            context.system.scheduler.scheduleOnce(Random.nextInt(3) second, self, AddPictureToAlbum)
          }
        }

        context.system.scheduler.scheduleOnce(Random.nextInt(3) second, self, false)
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
      val pipeline = sendReceive ~> unmarshal[Objects.Picture]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/picture", newPicture)
      }

      future onComplete {
        case Success(obj: Picture) =>
          context.system.scheduler.scheduleOnce(1 second, self, MakePost(photo, obj.baseObject.id))

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put picture post")
      }


    case MakePicture(albumID) =>
      //      val newPicture = Picture(BaseObject(), myBaseObj.id, -1, "filename.png", getImageBytes("/image.png"))
      val newPicture = Picture(BaseObject(), myBaseObj.id, -1, "filename.png", "blah")
      val pipeline = sendReceive ~> unmarshal[Objects.Picture]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/picture", newPicture)
      }

      future onComplete {
        case Success(obj: Picture) =>
        //          log.info(obj.toString())
        //          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePicture)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put picture")
      }

    case AddPictureToAlbum =>
      val pipeline = sendReceive ~> unmarshal[Objects.Album]
      val future = pipeline {
        pipelining.Get("http://" + serviceHost + ":" + servicePort + "/album/" + myBaseObj.id)
      }

      future onComplete {
        case Success(obj) =>
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePicture(obj.baseObject.id))

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put album")
      }


    case MakeAlbum =>
      val newAlbum = Album(baseObject = BaseObject(), myBaseObj.id, new DateTime().toString(), new DateTime().toString(), -1, "My new Album")
      val pipeline = sendReceive ~> unmarshal[Objects.Album]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/album", newAlbum)
      }

      future onComplete {
        case Success(obj) =>
          numAlbums += 1
        //          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeAlbum)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put album")
      }

    // From matchmaker
    case aNewFriend: ActorRef =>
      log.info(myBaseObj.id + " just met someone")
      aNewFriend ! Handshake(true, myBaseObj.id)

    // From new friend
    case Handshake(needResponse, id) =>
      myRealFriends.put(sender, id)
      self ! MakeFriend(id)
      if (needResponse) {
        sender ! Handshake(false, myBaseObj.id)
      }


    /*TODO check this*/
    case MakeFriend(id) =>
      val updatedList = UpdateFriendList(myBaseObj.id, if (id == -1) Random.nextInt(myBaseObj.id) else id)

      val pipeline = sendReceive ~> unmarshal[UpdateFriendList]
      val future = pipeline {
        pipelining.Post("http://" + serviceHost + ":" + servicePort + "/addfriend", updatedList)
      }

      future onComplete {
        case Success(obj: UpdateFriendList) =>
          myFriends.append(obj.fid)
        //          log.info("added friend", obj)
        //          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeFriend)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't post updated friend list")
      }


    case GetFriendsPost =>
      var friendId = Random.nextInt(myBaseObj.id)
      if (myFriends.nonEmpty) {
        friendId = myFriends(Random.nextInt(myFriends.length))
      }

      val pipeline = sendReceive ~> unmarshal[Post]
      val future = pipeline {
        pipelining.Get("http://" + serviceHost + ":" + servicePort + "/post/" + friendId)
      }

      future onComplete {
        case Success(obj: Post) =>
        //            context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, GetFriendsPost)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't get friend's post")
      }
    case (entity: HttpResponse, returnClass: String) =>
      returnClass match {
        case "user" =>
          me = entity ~> unmarshal[User]
          myBaseObj = me.baseObject
          context.system.scheduler.scheduleOnce(10 second, self, false)
          log.info("Printing {}", me)
        case "page" =>
          mePage = entity ~> unmarshal[Page]
          myBaseObj = mePage.baseObject
          context.system.scheduler.scheduleOnce(10 second, self, false)
          log.info("Printing {}", mePage)
        case "post" => numPosts += 1
        case "album" => numAlbums += 1
      }
  }

  def getOrDeleteObject(objType: String, id: Int, getOrDelete: Boolean) {
    val pipeline = objType match {
      case "Post" =>
        sendReceive ~> unmarshal[Objects.Post]
      case "Album" =>
        sendReceive ~> unmarshal[Objects.Album]
      case "Picture" =>
        sendReceive ~> unmarshal[Objects.Picture]
      case "FriendList" =>
        sendReceive ~> unmarshal[Objects.FriendList]
      case "User" =>
        sendReceive ~> unmarshal[Objects.User]
      case "Page" =>
        sendReceive ~> unmarshal[Page]
    }

    val future = getOrDelete match {
      case true =>
        pipeline {
          pipelining.Get("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase + "/" + id)
        }
      case false =>
        pipeline {
          pipelining.Delete("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase + "/" + id)
        }
    }

    future onComplete {
      case Success(obj: Object) =>
      //        println(if (getOrDelete) "Get " else "Delete" + objType, obj)

      case Success(somethingUnexpected) =>
        println("Unexpected return", somethingUnexpected)

      case Failure(error) =>
        println(error.getMessage, "Couldn't " + (if (getOrDelete) "get " else "delete ") + objType)
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

  def put(json: JsObject, returnClass: String) = {
    val pipeline = sendReceive

    val future = pipeline {
      pipelining.Put("http://" + serviceHost + ":" + servicePort + "/user", json)
    }

    future onComplete {
      case Success(somethingUnexpected) => self !(somethingUnexpected, returnClass)
      case Failure(error) => log.error(error, "Couldn't create user")
    }
  }
}