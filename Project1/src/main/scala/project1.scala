import akka.actor._
import com.typesafe.config.ConfigFactory

import scala.util.Random

object project1 extends App {
  val configStr = """
                    |akka {
                    |  actor {
                    |    provider = "akka.remote.RemoteActorRefProvider"
                    |  }
                    |  remote {
                    |    enabled-transports = ["akka.remote.netty.tcp"]
                    |    netty.tcp {
                    |      hostname = "127.0.0.1"
                    |      port = 2552
                    |    }
                    | }
                    |}
                  """.stripMargin
  val input = "4"
  //arg(0)
  val config = ConfigFactory.load(ConfigFactory.parseString(configStr))
  val system = ActorSystem(name = "BitcoinMiningSystem", config = config)

  // number of bitcoin strings that will be sent to a worker at a time
  // we need to change this, actually, work unit is not the number of strings being sent to each
  // worker, but some indication of where that workers assigment of work starts and ends
  // the reader class needs to be changed to be a WorkAssigner
  // This realization came one you see how the workers and reader interacts, they send each other messages
  // If reader is expected to generate the string, there may be times, where other workers are idle
  // Each worker should be given enough work without keeping the reader class too busy/ too relaxed is the ideal
  // work unit.
  val NUM_COINS_PER_WORKER = 5
  // constant that prefixes all bitcoins to be hashed
  val BITCOIN_STRING_PREFIX = "snair"
  //Get this machines IP
  //Get Master IP, set up actors that are required in the master machine
  val masterIP = if (input.matches("^\\d+$")) {
    val k = input.toInt
    system.actorOf(Props(new Reader(k = k, numberOfCoins = NUM_COINS_PER_WORKER, prefix = BITCOIN_STRING_PREFIX)),
      name = "reader")
    system.actorOf(Props[FindIndicator], name = "findIndicator")
    "127.0.0.1"
  } else {
    input
  }

  //Set up Worker
  system.actorOf(Props(new Worker(masterIP = masterIP)), name = "worker")

  Thread.sleep(180000)
  system.shutdown()
}

class Reader(k: Int, numberOfCoins: Int, prefix: String) extends Actor {
  val random = new Random()
  var numCoins: Long = 0
  var strLength:Long = 0
  var charIdx = 0
  val charList = (33 until 126).toList


  def receive = {
    case x: String =>
      println(x) //This will be the workers, ip address
      sender ! k
    case true =>
      numCoins = numCoins + numberOfCoins
      val coins = (0 until numberOfCoins).foldLeft(List[String]()) { (op, idx) =>
        val str = (0 to 52).foldLeft("") { (opStr, sIdx) =>
          opStr + random.nextPrintableChar()
        }
        prefix + str :: op
      }
      println(numCoins)
      sender ! Work(coins)

  }
}

class Worker(masterIP: String) extends Actor {
  val config = ConfigFactory.load()
  val port = config.getInt("akka.remote.netty.tcp.port")
  val reader = context.actorSelection(s"akka.tcp://BitcoinMiningSystem@$masterIP:$port/user/reader")
  val findIndicator = context.actorSelection(s"akka.tcp://BitcoinMiningSystem@$masterIP:$port/user/findIndicator")
  val thisIP = if (masterIP.equals("127.0.0.1")) masterIP else java.net.InetAddress.getLocalHost.getHostAddress
  var k: Int = _

  reader ! thisIP

  def MD5(s: String): String = {
    val m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
    m.map("%02x".format(_)).mkString
  }

  def receive = {
    case setK: Int => k = setK
      sender ! true
    // Receive some strings to hash from Reader
    case Work(bitcoins) =>
      val validCoins = bitcoins.foldLeft(List[Bitcoin]()) { (op, coin) =>
        val hash = MD5(coin)
        if (hash.substring(0, k).count(_ == '0') == k) {
          Bitcoin(bitcoinString = coin, bitcoinHash = hash) :: op
        } else {
          op
        }
      }

      // Send any valid coins to the Listener
      if (validCoins.nonEmpty) findIndicator ! Result(validCoins)
      sender ! true
  }
}

class FindIndicator extends Actor {
  def receive = {
    // Print all valid bitcoin returned from Worker
    case Result(bitcoins) => bitcoins.foreach(println)
  }
}

case class Bitcoin(bitcoinString: String, bitcoinHash: String) {
  override def toString: String = bitcoinString + "  " + bitcoinHash
}

sealed trait BitcoinMessage

case object Calculate extends BitcoinMessage

case class Work(potentialBitcoins: List[String]) extends BitcoinMessage

case class Result(bitcoins: List[Bitcoin]) extends BitcoinMessage