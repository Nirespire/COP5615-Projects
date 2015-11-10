import akka.actor.Props
import akka.util.Timeout
import spray.routing._
import spray.http._
import MediaTypes.`application/json`
import scala.concurrent.duration._

trait RootService extends HttpService {

  val storage = actorRefFactory.actorOf(Props[StorageActor], "storage")

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)

  val myRoute =
    respondWithMediaType(`application/json`) {
      path("profile") {
        post {
          complete {
            "Post profile"
          }
        } ~
          put {
            complete {
              "put profile"
            }
          }
      } ~
        path("profile" / IntNumber) { (profileId) =>
          get {
            requestContext =>
              val workerService = actorRefFactory.actorOf(Props(new WorkerActor(requestContext)))
              workerService ! profileId
          } ~
            delete {
              requestContext =>
            }
        }
    }
}