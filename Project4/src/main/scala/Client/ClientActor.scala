package Client

import java.io.InputStream

import Client.ClientType.ClientType
import Client.Messages._
import Client.Resources._
import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.PostType._
import Objects._
import akka.actor.{Actor, ActorLogging}
import com.google.common.io.BaseEncoding
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}


class ClientActor(isPage: Boolean = false, clientType: ClientType) extends Actor with ActorLogging {

  var myFriends = mutable.ArrayBuffer[Int]()

  var me: User = null
  var mePage: Page = null
  var myBaseObj: BaseObject = null

  val (putPercent, getPercent, friendPercent) = clientType match {
    case ClientType.Active => (80, 50, 90)
    case ClientType.Passive => (20, 90, 80)
    case ClientType.ContentCreator => (70, 20, 10)
  }

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  val myPages = mutable.ArrayBuffer[Page]()

  import context.dispatcher

  def receive = {
    // Create a user profile or page for self
    case true =>
      if (isPage) {
        val newPage = Page(BaseObject(), "about", pageCategories(Random.nextInt(pageCategories.length)), -1)
        val pipeline = sendReceive ~> unmarshal[Objects.Page]

        val future = pipeline {
          pipelining.Put("http://" + serviceHost + ":" + servicePort + "/page", newPage)
        }

        future onComplete {
          case Success(obj: Page) =>
            mePage = obj
            myBaseObj = obj.baseObject
            context.system.scheduler.scheduleOnce(10 second, self, false)

          case Success(somethingUnexpected) =>
            log.error("Unexpected return", somethingUnexpected)

          case Failure(error) =>
            log.error(error, "Couldn't create page")
        }
      }
      else {
        val newUser = User(BaseObject(), "about me", "04-25-1994", 'M', "Sanjay", "Nair")
        val pipeline = sendReceive ~> unmarshal[Objects.User]

        val future = pipeline {
          pipelining.Put("http://" + serviceHost + ":" + servicePort + "/user", newUser)
        }

        future onComplete {
          case Success(obj: User) =>
            me = obj
            myBaseObj = obj.baseObject
            context.system.scheduler.scheduleOnce(10 second, self, false)

          case Success(somethingUnexpected) =>
            log.error("Unexpected return", somethingUnexpected)

          case Failure(error) =>
            log.error(error, "Couldn't create user")
        }
      }

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
          context.system.scheduler.scheduleOnce(1 second, self, MakePost)
          context.system.scheduler.scheduleOnce(1 second, self, MakeAlbum)
          //context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePicture)
          //context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePage)
        }

        if (probability < getPercent) {
          //context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, GetFriendsPost)
        }

        if (probability < friendPercent) {
          context.system.scheduler.scheduleOnce(1 second, self, MakeFriend)
        }


      }

    case MakePost =>

      val newPost = Objects.Post(baseObject = BaseObject(), myBaseObj.id, new DateTime().toString(), myBaseObj.id, statuses(Random.nextInt(statuses.length)), status)
      val pipeline = sendReceive ~> unmarshal[Objects.Post]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/post", newPost)
      }

      future onComplete {
        case Success(obj: Post) =>
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePost)

        case Success(somethingUnexpected) =>
          println("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put post")
      }

    case MakePicture =>

      //      TODO: Need to assign this an album. Do get on albums first.
      val newPicture = Picture(BaseObject(), myBaseObj.id, -1, "filename.png", getImageBytes("/image.png"))
      val pipeline = sendReceive ~> unmarshal[Objects.Picture]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/picture", newPicture)
      }

      future onComplete {
        case Success(obj: Picture) =>
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePicture)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put picture")
      }


    case MakeAlbum =>

      val newAlbum = Album(baseObject = BaseObject(), myBaseObj.id, new DateTime().toString(), new DateTime().toString(), -1, "My new Album")
      val pipeline = sendReceive ~> unmarshal[Objects.Album]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/album", newAlbum)
      }

      future onComplete {
        case Success(obj) =>
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeAlbum)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put album")
      }


    case MakePage =>

      //      TODO Need to assign a picture cover photo
      val newPage = Page(baseObject = BaseObject(), "page description", "page category", -1)
      val pipeline = sendReceive ~> unmarshal[Objects.Page]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/page", newPage)
      }

      future onComplete {
        case Success(obj: Page) =>
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePage)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put page")
      }

    case MakeFriend =>
      val updatedList = UpdateFriendList(myBaseObj.id, Random.nextInt(myBaseObj.id))

      val pipeline = sendReceive ~> unmarshal[UpdateFriendList]
      val future = pipeline {
        pipelining.Post("http://" + serviceHost + ":" + servicePort + "/addfriend", updatedList)
      }

      future onComplete {
        case Success(obj: UpdateFriendList) =>
          //          log.info("added friend", obj)
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeFriend)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't post updated friend list")
      }


    case GetFriendsPost =>
      if (!myFriends.isEmpty) {
        //        TODO replace this with a get to some friendlist
        val friendId = myFriends(Random.nextInt(myFriends.length))

        val pipeline = sendReceive ~> unmarshal[Post]
        val future = pipeline {
          pipelining.Get("http://" + serviceHost + ":" + servicePort + "/post/" + friendId)
        }

        future onComplete {
          case Success(obj: Post) =>
            context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, GetFriendsPost)

          case Success(somethingUnexpected) =>
            log.error("Unexpected return", somethingUnexpected)

          case Failure(error) =>
            log.error(error, "Couldn't get friend's post")
        }
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
        println(error, "Couldn't " + (if (getOrDelete) "get " else "delete ") + objType)
    }
  }


  def getImageBytes(filename: String): String = {
    val is: InputStream = getClass.getResourceAsStream(filename)
    val bytes = Stream.continually(is.read).takeWhile(_ != -1).map(_.toByte).toArray
    BaseEncoding.base64().encode(bytes)
  }
}

