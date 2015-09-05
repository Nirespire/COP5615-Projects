import akka.actor._
import com.typesafe.config.ConfigFactory

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
  val input = "5"
  //arg(0)
  val config = ConfigFactory.load(ConfigFactory.parseString(configStr))
  val system = ActorSystem(name = "BitcoinMiningSystem", config = config)

  // number of bitcoin strings that will be sent to a worker at a time
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

  Thread.sleep(15000)
  system.shutdown()
}

class Reader(k: Int, numberOfCoins: Int, prefix: String) extends Actor {
  def receive = {
    case x: String =>
      println(x) //This will be the workers, ip address
      sender ! k
    case true =>
      val coins = (0 until numberOfCoins).foldLeft(List[String]()) { (op, idx) => prefix + "random" :: op }
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
  override def toString(): String = bitcoinString + "  " + bitcoinHash
}

sealed trait BitcoinMessage

case object Calculate extends BitcoinMessage

case class Work(potentialBitcoins: List[String]) extends BitcoinMessage

case class Result(bitcoins: List[Bitcoin]) extends BitcoinMessage