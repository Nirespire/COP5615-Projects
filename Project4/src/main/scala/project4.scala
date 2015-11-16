import Server.Actors.RootServerActor
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.duration._
import scala.util.Try

object project4 extends App {

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  // Start up actor system for server
  implicit val serverSystem = ActorSystem("fb-spray-system")
  val service = serverSystem.actorOf(Props[RootServerActor], "fb-REST-service")
  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = serviceHost, port = servicePort)

  /*
    Thread.sleep(1000)

    println("Start clients!")

    // Start up actor system of clients
    val clientSystem = ActorSystem("client-spray-system")

    (1 to 10).foreach { idx =>
      clientSystem.actorOf(Props(new Client.ClientActor(idx)), "client"+idx) ! true
    }

    println("End Loop")
  */

}
