import akka.actor.{ActorLogging, Actor, ActorSystem, Props}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.Try

object Project4 extends App {

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)

  // Start up actor system for server
  implicit val serverSystem = ActorSystem("fb-spray-system")
  val service = serverSystem.actorOf(Props[RootServerActor], "fb-REST-service")
  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = servicePort)

  Thread.sleep(5000)

  // Start up actor system of clients
  val clientSystem = ActorSystem("fb-spray-system")
  //val client = clientSystem.actorOf(Props[ClientActor], "client-actor")

  (1 to 1000).foreach { idx =>
    clientSystem.actorOf(Props(new ClientActor(idx)), "client"+idx) ! true
  }

}

class RootServerActor extends Actor with RootService with ActorLogging {
  def actorRefFactory = context
  def receive = runRoute(myRoute)
}