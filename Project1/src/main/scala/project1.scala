import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.util.Random
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
                    |      hostname = "127.0.0.1"
                    |      port = 2552
                    |    }
                    | }
                    |}
                  """.stripMargin
  val input = "1"
  //arg(0)
  val config = ConfigFactory.load(ConfigFactory.parseString(configStr))
  val appName = config.getString("app.name")
  // constant that prefixes all bitcoins to be hashed
  val prefix = config.getString("app.prefix")
  val port = config.getInt("akka.remote.netty.tcp.port")
  val workUnit = config.getInt("app.work_unit")
  val numOfWorkers = config.getInt("app.number_of_workers")
  val system = ActorSystem(name = appName, config = config)

  // Changed from NUMBER_OF_COINS to WORK_UNIT, actually, work unit is not the number of strings being sent to each
  // worker, but some indication of where that workers assigment of work starts and ends
  // the reader class needs to be changed to be a WorkAssigner
  // This realization came one you see how the workers and reader interacts, they send each other messages
  // If reader is expected to generate the string, there may be times, where other workers are idle
  // Each worker should be given enough work without keeping the reader class too busy/ too relaxed is the ideal
  // work unit.
  val (reader, findIndicator) = if (input.matches("^\\d+$")) {
    val k = input.toInt
    (system.actorOf(Props(new Reader(k = k)), name = "reader"),
      system.actorOf(Props[FindIndicator], name = "findIndicator"))
  } else {
    implicit val timeout = Timeout(5 seconds)
    (Await.result(system.actorSelection(s"akka.tcp://$appName@$input:$port/user/reader").
      resolveOne(), timeout.duration),
      Await.result(system.actorSelection(s"akka.tcp://$appName@$input:$port/user/findIndicator").
        resolveOne(), timeout.duration))
  }

  // Set up Workers
  (1 to numOfWorkers).foreach { idx =>
    system.actorOf(Props(new Worker(reader = reader, findIndicator = findIndicator,
      prefix = prefix, workUnit = workUnit)), name = s"worker$idx")
  }

  Thread.sleep(10000)
  system.shutdown()
}

class Reader(k: Int) extends Actor {
  val random = new Random()
  var numCoins: Long = 0
  var strLength: Long = 0
  var charIdx = 0
  val numberOfCoins = 5

  def receive = {
    case x: String =>
      println(x) //This will be the workers, ip address
    case false => sender ! k
    case true =>
      //Instead send a seed string, and each worker will have to try say adding all combinations
      // of three chars appended to given seed,
      // example,if WORK_UNIT is 3 & the seed send to the worker is "a", worker is expected to try everything
      // from snairaaaa to sanirazzz, in this way we are assigning each worker a unique set of stuff to try
      // among themselves.
      // The only exception is when a blank seed ("") is  sent to a worker, that work will try all
      // 0 to 3 length string combinations , e.g. snair, snaira, sanirb,... sanirzzz.

      //We need to see which work unit i.e. number of combinations(2,3,4, or more) should
      // each worker try, and which is most efficient.

      val coins = (0 until numberOfCoins).foldLeft(List[String]()) { (op, idx) =>
        val str = (0 to 52).foldLeft("") { (opStr, sIdx) =>
          opStr + random.nextPrintableChar()
        }
        str :: op
      }

      println(sender)
      sender ! Work(coins)
  }
}

class Worker(reader: ActorRef, findIndicator: ActorRef, prefix: String, workUnit: Int) extends Actor {
  var k: Int = _
  // List of visible ASCII chars
  val charList = (33 until 126).toList

  reader ! false

  def MD5(s: String): String = {
    val m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
    m.map("%02x".format(_)).mkString
  }

  def receive = {
    case setK: Int => k = setK
      sender ! true
    // Receive some strings to hash from Reader
    case seed: String =>
    //
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