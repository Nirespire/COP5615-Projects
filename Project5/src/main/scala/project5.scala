import Server.Actors.RootServerActor
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.duration._
import scala.util.Try

object project5 extends App {
  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")
  lazy val numClients = Try(config.getInt("client.numClients")).getOrElse(0)

  // Start up actor system for server
  implicit val serverSystem = ActorSystem("fb-spray-system")
  val service = serverSystem.actorOf(Props[RootServerActor], "fb-REST-service")
  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = serviceHost, port = servicePort)

  Thread.sleep(1000)
}
