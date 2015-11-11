import Objects.ObjectTypes.PostType._
import Objects.Post
import akka.actor.Actor
import spray.client.pipelining
import spray.client.pipelining._
import scala.util.{Success, Failure}
import Objects.PostJsonSupport._

class ClientActor(id:Int) extends Actor {

  import context.dispatcher

  def receive = {
    case true =>
      val pipeline = sendReceive ~> unmarshal[Post]

      val postFuture = pipeline{
        val newPost = Post(id=id, creator=456, createdTime="now", from=456, message="testpost", postType=link)
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

}
