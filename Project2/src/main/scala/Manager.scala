import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import messages.{StartGossip, Algorithm, Setup, StartPushSum, InitialGossip, SetupGossip}

import scala.util.Random
import scala.collection.mutable


class Manager(numNodes: Int, algorithm: Algorithm.Value) extends Actor {
  val config = ConfigFactory.load()
  var completedCounter = 0
  var setupCounter = 0
  val random = new Random()

  var startTime = System.nanoTime()

  def timePassed = (System.nanoTime() - startTime) / Math.pow(10, 9)

  def completed = completedCounter == numNodes

  def setupCompleted = setupCounter == numNodes

  // referenced from
  // http://alvinalexander.com/scala/creating-random-strings-in-scala
  def randomString(n: Int): String = {
    n match {
      case 1 => random.nextPrintableChar.toString
      case _ => random.nextPrintableChar.toString ++ randomString(n - 1).toString
    }
  }


  def receive = {
    case false =>
      setupCounter += 1
    //debug
    //      completedCounter += 1
    //      println(sender + "==================" + setupCounter)
    case (true, 1) => completedCounter -= 1
    case true => completedCounter += 1
      //debug
      //      println(sender + " ---- " + completedCounter)
      if (completed) {
        println("Convergence in " + timePassed)
        context.system.shutdown()
      }
    case Setup =>
      //      println("Manager -               ---- START")
      (1 to numNodes).foreach { idx =>
        context.actorSelection(s"/user/node$idx") ! Setup
      }
      algorithm match {
        case Algorithm.pushSum => self ! StartPushSum
        case Algorithm.gossip => self ! StartGossip
      }
    case StartPushSum =>
      if (!setupCompleted) {
        self ! StartPushSum
      } else {
        println("Setup completed in " + timePassed)
        val randIdx = random.nextInt(numNodes) + 1
        startTime = System.nanoTime()
        context.actorSelection(s"/user/node$randIdx") ! StartPushSum
      }
    case StartGossip =>
      if (!setupCompleted) {
        self ! StartGossip
      } else {
        println("Setup completed in " + timePassed)

        var randIdx = random.nextInt(numNodes) + 1
        val randIdxs = mutable.Set[Int]()

        for (i <- 1 to config.getInt("app.numGossipWords")) {
          val randString = randomString(config.getInt("app.gossipStringLength"))
          //debug
          //        println("Gossip is: " + randString)
          context.actorSelection(s"/user/node$randIdx") ! SetupGossip(randString)
          randIdxs.add(randIdx)
          randIdx = random.nextInt(numNodes) + 1
        }

        //debug
        //println(randIdxs.mkString(","))
        startTime = System.nanoTime()
        randIdxs.foreach(i => context.actorSelection(s"/user/node$i") ! InitialGossip)


      }
  }
}
