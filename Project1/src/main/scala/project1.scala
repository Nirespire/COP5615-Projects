import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

object project1 extends App {
  val configStr = """
                    |app.name = "BitcoinMiningSystem"
                    |app.prefix = "snair"
                    |app.work_unit = 3
                    |app.number_of_workers = 5
                    |akka {
                    |  actor {
                    |    provider = "akka.remote.RemoteActorRefProvider"
                    |  }
                    |  remote {
                    |    enabled-transports = ["akka.remote.netty.tcp"]
                    |    netty.tcp {
                    |      port = 2552
                    |    }
                    | }
                    |}
                  """.stripMargin

  val input = args(0)
  val ipStr = "akka.remote.netty.tcp.hostname = \"" + java.net.InetAddress.getLocalHost.getHostAddress + "\""
  val config = ConfigFactory.load(ConfigFactory.parseString(configStr + ipStr))
  val appName = config.getString("app.name")
  val prefix = config.getString("app.prefix")
  val port = config.getInt("akka.remote.netty.tcp.port")
  val workUnit = config.getInt("app.work_unit")
  val numOfWorkers = config.getInt("app.number_of_workers")

  val system = ActorSystem(name = appName, config = config)
  val (workAssigner, findIndicator) = if (input.matches("^-?\\d+$")) {
    // If args(0) is a number, start the master system
    val k = input.toInt
    (system.actorOf(Props(new WorkAssigner(k = k)), name = "workAssigner"),
      system.actorOf(Props(new FindIndicator(largest = k < 0)), name = "findIndicator"))
  } else {
    // Else, it's an IP address, so start a remote worker system and connect to the input IP
    implicit val timeout = Timeout(5 seconds)
    val dur = timeout.duration
    (Await.result(system.actorSelection(s"akka.tcp://$appName@$input:$port/user/workAssigner").resolveOne(), dur),
      Await.result(system.actorSelection(s"akka.tcp://$appName@$input:$port/user/findIndicator").resolveOne(), dur))
  }

  // Set up Workers
  (1 to numOfWorkers).foreach { idx =>
    system.actorOf(Props(new Worker(workAssigner = workAssigner, findIndicator = findIndicator,
      prefix = prefix, workUnit = workUnit)), name = s"worker$idx")
  }

  // Run for some amount of time
  Thread.sleep(60000)
  system.shutdown()
}

object StringFunctions {
  // Using the visible range of ASCII characters up till DEL 127
  val startIdx = 32
  val stopIdx = 126
  val startChar = startIdx.toChar
  val startString = startChar.toString
  val stopChar = stopIdx.toChar
  val stopString = stopChar.toString

  /*
   * Provided any string 'a', will return the ascii equivalent of adding 1 to the last character
   * For example (assumming a-z is your ascii range):
   *  plus("aaa") === "aab"
   *  plus("zzz") === "aaaa"
   */
  def plus(a: String): String = {
    if (a.last == stopChar) {
      plus(a.substring(0, a.length - 1)) + startChar
    } else {
      a.substring(0, a.length - 1) + (a.last + 1).toChar
    }
  }

  def getNextCombo(a: String) = {
    if (a.isEmpty) {
      startString
    } else if (a.forall(_ == stopChar)) {
      startString * (a.length + 1)
    } else {
      plus(a)
    }
  }

  def countNumLeadingZeroes(input: String): Integer = {
    if (input.charAt(0) != '0') {
      0
    } else {
      1 + countNumLeadingZeroes(input.substring(1, input.length))
    }
  }
}

class WorkAssigner(k: Int) extends Actor {
  var seed = ""

  def receive = {
    case false => sender ! k
    case true =>
      sender ! seed
      seed = StringFunctions.getNextCombo(seed)
  }
}

class Worker(workAssigner: ActorRef, findIndicator: ActorRef, prefix: String, workUnit: Int) extends Actor {
  var k: Int = 0
  var largestNumZeroes = 0
  workAssigner ! false

  // Bitcoin hash function
  @inline def SHA256(s: String): String = {
    val m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
    m.map("%02x".format(_)).mkString
  }

  def receive = {
    case setK: Int =>
      k = setK
      sender ! true
    case seed: String =>
      var postFix = if (seed.isEmpty) "" else StringFunctions.startString * workUnit
      while (postFix != StringFunctions.startString * (workUnit + 1)) {
        val coin = prefix + seed + postFix
        val hash = SHA256(coin)
        if (k >= 0) {
          if (hash.substring(0, k).count(_ == '0') == k) {
            findIndicator ! Bitcoin(bitcoinString = coin, bitcoinHash = hash)
          }
        } else {
          /* compare with local max first */
          val numLeadingZeroes = StringFunctions.countNumLeadingZeroes(hash)
          if (numLeadingZeroes > largestNumZeroes) {
            largestNumZeroes = numLeadingZeroes
            findIndicator ! Bitcoin(bitcoinString = coin, bitcoinHash = hash)
          }
        }

        postFix = StringFunctions.getNextCombo(postFix)
      }

      sender ! true
  }
}

class FindIndicator(largest: Boolean) extends Actor {
  var largestNumZeroes = 0

  def receive = {
    // Print all valid bitcoin returned from Worker
    case bitcoin: Bitcoin =>
      if (largest) {
        val numZeroes = StringFunctions.countNumLeadingZeroes(bitcoin.bitcoinHash)
        if (numZeroes > largestNumZeroes) {
          largestNumZeroes = numZeroes
          println(/*sender + "\t" + */ bitcoin)
        }
      } else {
        println(/*sender + "\t" + */ bitcoin)
      }
  }
}

case class Bitcoin(bitcoinString: String, bitcoinHash: String) {
  override def toString: String = s"$bitcoinString\t$bitcoinHash"
}