import akka.actor._
import akka.routing.RoundRobinPool
import scala.concurrent.duration._

object project1 extends App {

  sealed trait BitcoinMessage
  case object Calculate extends BitcoinMessage
  case class Work(bitcoins: Array[String]) extends BitcoinMessage
  case class Result(bitcoins: Array[String]) extends BitcoinMessage

  var IPRegex = """^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"""
  var NUM_COINS_PER_WORKER = 5

  if(args(0).matches(IPRegex)){
    startSlaveSystem(args(0))
  }
  else{
    startMasterSystem(k = Integer.parseInt(args(0)), numCoinsPerWorker = NUM_COINS_PER_WORKER)
  }

  def startMasterSystem(k : Int, numCoinsPerWorker : Int){
    println("Start Master System")
    println("K = " + k)
    println("Coins per worker = " + numCoinsPerWorker)

    val system = ActorSystem("BitcoinMasterSystem")

    system.shutdown()
  }

  def startSlaveSystem(IPAddress : String){
    println("Start Slave System")
    println("IP = " + IPAddress)

    val system = ActorSystem("BitcoinSlaveSystem")

    system.shutdown()
  }


  class Reader(k : Int, numberOfCoins : Int) extends Actor {

    // need to reconsider for growing pool of workers
    //val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

    def receive = {
      case Calculate =>
    }

  }

  class Worker(k : Int, listner : ActorRef) extends Actor {

    def MD5(s: String): String = {
        val m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
        m.map("%02x".format(_)).mkString
    }

    def receive = {
      case Work(bitcoins) =>
        val validCoins = Array[String]()

        for(coin <- bitcoins) {
          if (MD5(coin).substring(0, k).count(_ == '0') == k)
            validCoins :+ coin
        }

        listner ! Result(validCoins)

      }
  }

  class Listener extends Actor {
    def receive = {
      case Result(bitcoins) =>
    }
  }
}