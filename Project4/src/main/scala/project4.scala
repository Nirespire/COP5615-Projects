import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import spray.http._
import spray.client.pipelining._
import scala.concurrent.Future
import scala.concurrent.duration._

object Project4 extends App {

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)

  // we need an ActorSystem to host our application in
  implicit val serverSystem = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = serverSystem.actorOf(Props[MyServiceActor], "demo-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = servicePort)

  
//  implicit val clientSystem = ActorSystem("on-spray-client")
//  import clientSystem.dispatcher // execution context for futures
//
//  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
//
//  val response: Future[HttpResponse] = pipeline(Get("http://spray.io/"))

}