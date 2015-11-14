package Client

import Objects._
import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import spray.client.pipelining
import spray.client.pipelining._
import scala.collection.mutable
import scala.util.{Try, Failure, Success}
import ObjectJsonSupport._
import Objects.ObjectTypes.PostType._

class ClientActor(id:Int) extends Actor {

  var myPosts = mutable.HashMap[Int,Post]()
  var myPictures = mutable.HashMap[Int,Picture]()
  var myAlbums = mutable.HashMap[Int,Album]()
  var myFriendLists = mutable.HashMap[Int,FriendList]()
  var myPages = mutable.HashMap[Int,Page]()

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getInt("service.host")).getOrElse("localhost")

  import context.dispatcher

  def receive = {
    case true =>
      val pipeline = sendReceive ~> unmarshal[Post]

      val postFuture = pipeline{
        val newPost = Objects.Post(id=id, creator=456, createdTime="now", from=456, message="testpost", postType=link)
        pipelining.Put("http://localhost:8080/post", newPost)
      }

      postFuture onComplete{
        case Success(post:Post) =>
          println("Posted the post", post)

          val getFuture = pipeline {
            pipelining.Get("http://localhost:8080/post/"+id)
          }

          getFuture onComplete {
            case Success(post:Post) =>
              println("Get post", post)


            case Success(somethingUnexpected) =>
              println("Unexpected return", somethingUnexpected)


            case Failure(error) =>
              println(error, "Couldn't get post")
          }


        case Success(somethingUnexpected) =>
          println("Unexpected return", somethingUnexpected)


        case Failure(error) =>
          println(error, "Couldn't post post")
      }
  }


  def getObject(objType:String, id:Int){
    val pipeline = objType match{
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

    val getFuture = pipeline {
      pipelining.Get("http://" + serviceHost + ":" + servicePort + "/"+ objType.toLowerCase +"/"+id)
    }

    getFuture onComplete {
      case Success(obj:Object) =>
        println("Get " + objType, obj)

      case Success(somethingUnexpected) =>
        println("Unexpected return", somethingUnexpected)

      case Failure(error) =>
        println(error, "Couldn't get " + objType)
    }
  }

  def deleteObject(objType:String, id:Int){

  }

  def postObject(obj:Object, id:Int){

  }

}
