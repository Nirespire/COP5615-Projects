import akka.actor._
import akka.routing.RoundRobinPool
import scala.concurrent.duration._

object project1 extends App {

  class Bitcoin(bitcoinString : String, bitcoinHash : String){
    var bc : String = bitcoinString
    var hs : String = bitcoinHash

    override def toString(): String = bc + "  " + hs

  }

  sealed trait BitcoinMessage
  case object Calculate extends BitcoinMessage
  case class Work(potentialBitcoins: Array[String]) extends BitcoinMessage
  case class Result(bitcoins: Array[Bitcoin]) extends BitcoinMessage

  var IPRegex = """^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"""

  // number of bitcoin strings that will be sent to a worker at a time
  var NUM_COINS_PER_WORKER = 5

  // constant that prefixes all bitcoins to be hashed
  var BITCOIN_STRING_PREFIX = "snair"

  // if an IP is provided, connect to the existing Master system as a new Worker
  if(args(0).matches(IPRegex)){
    startSlaveSystem(args(0))
  }
  // else start the Master system
  else{
    startMasterSystem(k = Integer.parseInt(args(0)), numCoinsPerWorker = NUM_COINS_PER_WORKER)
  }

  def startMasterSystem(k : Int, numCoinsPerWorker : Int){
    println("Start Master System")
    println("K = " + k)
    println("Coins per worker = " + numCoinsPerWorker)

    // Generator of random strings
    // List to keep track of generated strings

    val system = ActorSystem("BitcoinMasterSystem")

    val listener = system.actorOf(Props[Listener], name = "listener")

    val reader = system.actorOf(Props(new Reader(k = k, numberOfCoins = numCoinsPerWorker, listener = listener)), name = "reader")

    reader ! Calculate

    system.shutdown()
  }

  def startSlaveSystem(IPAddress : String){
    println("Start Slave System")
    println("IP = " + IPAddress)

    val system = ActorSystem("BitcoinSlaveSystem")

    system.shutdown()
  }


  class Reader(k : Int, numberOfCoins : Int, listener : ActorRef) extends Actor {

    // need to reconsider for growing pool of workers

    def receive = {
      case Calculate =>
        println("starting workers")
        // Start the workers
        //val workerRouter = context.actorOf(Props(new Worker(k=k, listener = listener)).withRouter(RoundRobinPool(4)), name = "workerRouter")
        //for(i <- 0 until 10) workerRouter ! Work(Array("random"))

    }

  }

  class Worker(k : Int, listener : ActorRef) extends Actor {

    def MD5(s: String): String = {
        val m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
        m.map("%02x".format(_)).mkString
    }

    def receive = {
      // Receive some strings to hash from Reader
      case Work(bitcoins) =>
        val validCoins = Array[Bitcoin]()

        for(coin <- bitcoins) {
          val hash = MD5(coin)
          if (hash.substring(0, k).count(_ == '0') == k)
            validCoins :+ new Bitcoin(bitcoinString = coin, bitcoinHash = hash)
        }

        // Send any valid coins to the Listener
        if(!validCoins.isEmpty) listener ! Result(validCoins)

      }
  }

  class Listener extends Actor {
    def receive = {
      // Print all valid bitcoin returned from Worker
      case Result(bitcoins) =>
        for(coin <- bitcoins){
          println(coin)
        }
    }
  }
}