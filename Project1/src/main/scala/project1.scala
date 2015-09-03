import akka.actor._
import akka.routing.RoundRobinPool
import scala.concurrent.duration._

object project1 extends App {

	class Worker extends Actor {

		def MD5(s: String): String = {
		    val m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
		    m.map("%02x".format(_)).mkString
		}

		def receive = {
			case Work(start, nrOfElements) =>
    	}
	}

	class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef) extends Actor {


		val workerRouter = context.actorOf(
			Props[Worker].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

		def receive = {
			case Calculate =>
			case Result(value) =>
		}

	}

	class Listener extends Actor {
		def receive = {
			case Work(start, nrOfElements) =>
		}
	}
}