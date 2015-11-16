package Client

import Client.Messages.{MakeFriendList, MakeAlbum, MakePost}
import Client.Resources.statuses
import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.PostType._
import Objects.ObjectTypes.ListType._
import Objects._
import Server.Messages.ResponseMessage
import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._
import spray.json._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}

class ClientActor(id: Int) extends Actor {

  var myPosts = mutable.HashMap[Int, Post]()
  var myPictures = mutable.HashMap[Int, Picture]()
  var myAlbums = mutable.HashMap[Int, Album]()
  var myFriendLists = mutable.HashMap[Int, FriendList]()
  var myPages = mutable.HashMap[Int, Page]()

  var me: User = _

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  import context.dispatcher

  def receive = {
    // Create a user profile for self
    case true =>
      putOrPostObject(User(b=BaseObject(), "about", "04-25-1994", 'M', "Sanjay", "Nair"), true)
      context.system.scheduler.scheduleOnce(1 second, self, false)

    case false =>
      if(me == null){
        context.system.scheduler.scheduleOnce(1 second, self, false)
      }
      else {
//        println("starting activity")
        getOrDeleteObject("User", me.b.id, true)
        context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePost)
        context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeAlbum)
        context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeFriendList)
      }

    case MakePost =>
      putOrPostObject(Objects.Post(b=BaseObject(), me.b.id, new DateTime().toString(), id, statuses(Random.nextInt(statuses.length)), status), true)
      context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePost)

    case MakeAlbum =>
      putOrPostObject(Objects.Album(b=BaseObject(),me.b.id,new DateTime().toString(),new DateTime().toString(),-1,"My new Album", Array[Int]()), true)
      context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeAlbum)

    case MakeFriendList =>
      putOrPostObject(Objects.FriendList(-1,me.b.id,Array[Int](),close_friends), true)
      context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeFriendList)


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

  def putOrPostObject(obj: Any, putOrPost: Boolean) {
    val (pipeline, objType) = obj match {
      case obj: Post =>
        (sendReceive ~> unmarshal[Objects.Post], "Post")
      case obj: Album =>
        (sendReceive ~> unmarshal[Objects.Album], "Album")
      case obj: Picture =>
        (sendReceive ~> unmarshal[Objects.Picture], "Picture")
      case obj: FriendList =>
        (sendReceive ~> unmarshal[Objects.FriendList], "FriendList")
      case obj: User =>
        (sendReceive ~> unmarshal[Objects.User], "User")
      case obj: Page =>
        (sendReceive ~> unmarshal[Page], "Page")
    }

    // TODO: Uglyness. Is there way to determine type outside case?
    val future = obj match {
      case obj: Post =>
        putOrPost match {
          case true =>
            pipeline {
              pipelining.Put("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
          case false =>
            pipeline {
              pipelining.Post("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
        }

      case obj: Album =>
        putOrPost match {
          case true =>
            pipeline {
              pipelining.Put("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
          case false =>
            pipeline {
              pipelining.Post("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
        }
      case obj: Picture =>
        putOrPost match {
          case true =>
            pipeline {
              pipelining.Put("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
          case false =>
            pipeline {
              pipelining.Post("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
        }
      case obj: FriendList =>
        putOrPost match {
          case true =>
            pipeline {
              pipelining.Put("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
          case false =>
            pipeline {
              pipelining.Post("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
        }
      case obj: User =>
        putOrPost match {
          case true =>
            pipeline {
              pipelining.Put("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
          case false =>
            pipeline {
              pipelining.Post("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
        }
      case obj: Page =>
        putOrPost match {
          case true =>
            pipeline {
              pipelining.Put("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
          case false =>
            pipeline {
              pipelining.Post("http://" + serviceHost + ":" + servicePort + "/" + objType.toLowerCase, obj)
            }
        }
    }

    future onComplete {
      case Success(obj: Object) =>
        //        println(if (putOrPost) "Put " else "Post " + objType, obj)

        if (obj.isInstanceOf[Post])
          myPosts.put(obj.asInstanceOf[Post].b.id, obj.asInstanceOf[Post])
        else if (obj.isInstanceOf[Album])
          myAlbums.put(obj.asInstanceOf[Album].b.id, obj.asInstanceOf[Album])
        else if (obj.isInstanceOf[FriendList])
          myFriendLists.put(obj.asInstanceOf[FriendList].id, obj.asInstanceOf[FriendList])
        else if (obj.isInstanceOf[Page])
          myPages.put(obj.asInstanceOf[Page].b.id, obj.asInstanceOf[Page])
        else if (obj.isInstanceOf[Picture])
          myPictures.put(obj.asInstanceOf[Picture].b.id, obj.asInstanceOf[Picture])
        else if (obj.isInstanceOf[User]) {
          me = obj.asInstanceOf[User]
//          println("registered as user " + me.b.id)
        }
        else if (obj.isInstanceOf[ResponseMessage])
          println("Response: " + obj.asInstanceOf[ResponseMessage].message)

      case Success(somethingUnexpected) =>
        println("Unexpected return", somethingUnexpected)

      case Failure(error) =>
        println(error, "Couldn't " + (if (putOrPost) "put " else "post ") + objType)
    }
  }
}

