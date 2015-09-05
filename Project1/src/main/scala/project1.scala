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
  val config = ConfigFactory.load(ConfigFactory.parseString(configStr))
  //arg(0)
  val system = ActorSystem(name = "BitcoinMiningSystem", config = config)

  // number of bitcoin strings that will be sent to a worker at a time
  val NUM_COINS_PER_WORKER = 5
  // constant that prefixes all bitcoins to be hashed
  val BITCOIN_STRING_PREFIX = "snair"
  //Get this machines IP
  //Get Master IP, set up actors that are required in the master machine
  val masterIP = if (input.matches("^\\d+$")) {
    val k = input.toInt
    val r = system.actorOf(Props(new Reader(k = k, numberOfCoins = NUM_COINS_PER_WORKER)), name = "reader")
    r ! "setup"
    system.actorOf(Props[FindIndicator], name = "findIndicator")
    println(r)
    "127.0.0.1"
  } else {
    input
  }

  //Set up Worker
  system.actorOf(Props(new Worker(masterIP = masterIP)), name = "worker")

  Thread.sleep(2000)
  system.shutdown()
}

class Reader(k: Int, numberOfCoins: Int) extends Actor {
  def receive = {
    case x: String => println(x) //This will be the workers, ip address
    case Calculate =>
      println("starting workers")
    // Start the workers
    //val workerRouter = context.actorOf(Props(new Worker(k=k, listener = listener)).withRouter(RoundRobinPool(4)), name = "workerRouter")
    //for(i <- 0 until 10) workerRouter ! Work(Array("random"))

  }

}

class Worker(masterIP: String) extends Actor {
  val config = ConfigFactory.load()
  val port = config.getInt("akka.remote.netty.tcp.port")
  println(port)
  println(masterIP)
  val reader = context.actorSelection(s"akka.tcp://BitcoinMiningSystem@$masterIP:$port/user/reader")
  val findIndicator = context.actorSelection(s"akka.tcp://BitcoinMiningSystem@$masterIP:$port/user/findIndicator")
  val thisIP = if (masterIP.equals("127.0.0.1")) masterIP else java.net.InetAddress.getLocalHost.getHostAddress
  println(reader)
  reader ! thisIP
  var k: Int = _

  def MD5(s: String): String = {
    val m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
    m.map("%02x".format(_)).mkString
  }

  def receive = {
    case setK: Int => k = setK
    // Receive some strings to hash from Reader
    case Work(bitcoins) =>
      val validCoins = Array[Bitcoin]()

      for (coin <- bitcoins) {
        val hash = MD5(coin)
        if (hash.substring(0, k).count(_ == '0') == k)
          validCoins :+ new Bitcoin(bitcoinString = coin, bitcoinHash = hash)
      }

      // Send any valid coins to the Listener
      if (!validCoins.isEmpty) findIndicator ! Result(validCoins)
  }
}

class FindIndicator extends Actor {
  def receive = {
    // Print all valid bitcoin returned from Worker
    case Result(bitcoins) =>
      for (coin <- bitcoins) {
        println(coin)
      }
  }
}

class Bitcoin(bitcoinString: String, bitcoinHash: String) {
  var bc: String = bitcoinString
  var hs: String = bitcoinHash

  override def toString(): String = bc + "  " + hs

}

sealed trait BitcoinMessage

case object Calculate extends BitcoinMessage

case class Work(potentialBitcoins: Array[String]) extends BitcoinMessage

case class Result(bitcoins: Array[Bitcoin]) extends BitcoinMessage