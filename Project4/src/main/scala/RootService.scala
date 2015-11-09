import akka.actor.Props
import akka.util.Timeout
import spray.routing._
import spray.http._
import MediaTypes._
import scala.concurrent.duration._

trait RootService extends HttpService {

  val worker = actorRefFactory.actorOf(Props[WorkerActor], "worker")

  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val myRoute =
    path("profile"){
      get{
        respondWithMediaType(`application/json`){
          complete{
            "get profile"
          }
        }
      } ~
        post {
          respondWithMediaType(`application/json`) {
            complete {
              "post profile"
            }
          }
        } ~
        put{
          respondWithMediaType(`application/json`){
            complete{
              "put profile"
            }
          }
        } ~
        delete {
          respondWithMediaType(`application/json`){
            complete{
              "delete profile"
            }
          }
        }
    }
}