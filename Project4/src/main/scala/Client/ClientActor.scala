package Client

import java.io.InputStream

import Objects._
import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._
import scala.collection.mutable
import scala.util.{Random, Try, Failure, Success}
import ObjectJsonSupport._
import Objects.ObjectTypes.PostType._
import Client.Resources.statuses

class ClientActor(id: Int) extends Actor {

  var myPosts = mutable.HashMap[Int, Post]()
  var myPictures = mutable.HashMap[Int, Picture]()
  var myAlbums = mutable.HashMap[Int, Album]()
  var myFriendLists = mutable.HashMap[Int, FriendList]()
  var myPages = mutable.HashMap[Int, Page]()

  var me:User = _

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  import context.dispatcher

  def receive = {
    case true =>
      putOrPostObject(Objects.Post(-1,id,new DateTime().toString(),id,statuses(Random.nextInt(statuses.length)),status),true)

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
        //println(if (getOrDelete) "Get " else "Delete" + objType, obj)

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
    val future = obj match{
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

        if(obj.isInstanceOf[Post])
          myPosts.put(obj.asInstanceOf[Post].id, obj.asInstanceOf[Post])
        else if(obj.isInstanceOf[Album])
          myAlbums.put(obj.asInstanceOf[Album].id, obj.asInstanceOf[Album])
        else if(obj.isInstanceOf[FriendList])
          myFriendLists.put(obj.asInstanceOf[FriendList].id, obj.asInstanceOf[FriendList])
        else if(obj.isInstanceOf[Page])
          myPages.put(obj.asInstanceOf[Page].id, obj.asInstanceOf[Page])
        else if(obj.isInstanceOf[Picture])
          myPictures.put(obj.asInstanceOf[Picture].id, obj.asInstanceOf[Picture])
        else if(obj.isInstanceOf[User])
          me = obj.asInstanceOf[User]

      case Success(somethingUnexpected) =>
        println("Unexpected return", somethingUnexpected)

      case Failure(error) =>
        println(error, "Couldn't " + (if (putOrPost) "put " else "post ") + objType)
    }
  }
}

