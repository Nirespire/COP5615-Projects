package Client

import Client.Messages._
import Client.Resources.statuses
import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.PostType._
import Objects.ObjectTypes.ListType._
import Objects._
import Server.Messages.ResponseMessage
import akka.actor.{ActorLogging, Actor}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._
import spray.json._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}

class ClientActor(id: Int) extends Actor with ActorLogging {

  var myFriends = mutable.ArrayBuffer[Int]()

  var me: User = null

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  val myPages = mutable.ArrayBuffer[Page]()

  import context.dispatcher

  def receive = {
    // Create a user profile for self
    case true =>
      val newUser = User(BaseObject(),"about me", "04-25-1994",'M',"Sanjay", "Nair")
      val pipeline = sendReceive ~> unmarshal[Objects.User]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/user", newUser)
      }

      future onComplete {
        case Success(obj: User) =>
          me = obj
          context.system.scheduler.scheduleOnce(10 second, self, false)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't create user")
      }


    case false =>
      if (me == null) {
        context.system.scheduler.scheduleOnce(1 second, self, false)
      }
      else {
//        TODO add some statistical parameter that chooses when each of these happen
//        log.info(me.b.id + " starting activity")
        getOrDeleteObject("User", me.b.id, true)
        context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePost)
        context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeAlbum)
//        context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePage)
        if(me.b.id > 100){
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeFriend)
//          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, GetFriendsPost)
        }

      }

    case MakePost =>

      val newPost = Objects.Post(b = BaseObject(), me.b.id, new DateTime().toString(), id, statuses(Random.nextInt(statuses.length)), status)
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
      val newPicture = Picture(BaseObject(), me.b.id, -1, "filename.png")
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

      val newAlbum = Album(b = BaseObject(), me.b.id, new DateTime().toString(), new DateTime().toString(), -1, "My new Album", Array[Int]())
      val pipeline = sendReceive ~> unmarshal[Objects.Album]
      val future = pipeline {
        pipelining.Put("http://" + serviceHost + ":" + servicePort + "/album", newAlbum)
      }

      future onComplete {
        case Success(obj: Album) =>
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeAlbum)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't put album")
      }


    case MakePage =>

//      TODO Need to assign a picture cover photo
      val newPage = Page(b = BaseObject(), "page description", "page category", -1)
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
      val updatedList = UpdFriendList(me.b.id,Random.nextInt(me.b.id))

      val pipeline = sendReceive ~> unmarshal[UpdFriendList]
      val future = pipeline {
        pipelining.Post("http://" + serviceHost + ":" + servicePort + "/addfriend", updatedList)
      }

      future onComplete {
        case Success(obj: UpdFriendList) =>
          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeFriend)

        case Success(somethingUnexpected) =>
          log.error("Unexpected return", somethingUnexpected)

        case Failure(error) =>
          log.error(error, "Couldn't post updated friend list")
      }


    case GetFriendsPost =>
      if(!myFriends.isEmpty) {
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
}

