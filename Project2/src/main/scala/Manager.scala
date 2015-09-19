import akka.actor.Actor
import messages.{StartGossip, Algorithm, Setup, StartPushSum}

import scala.util.Random

class Manager(numNodes: Int, algorithm: Algorithm.Value) extends Actor {
  var completedCounter = 0
  var setupCounter = 0
  val random = new Random()

  var startTime = -1.toLong

  def completed = completedCounter == numNodes

  def setupCompleted = setupCounter == numNodes

  def receive = {
    case false =>
      setupCounter += 1
      println(sender + "==================" + setupCounter)
    case true => completedCounter += 1
      println(sender + " ---- " + completedCounter)
      if (completed) {
        println("Convergence in " + (System.currentTimeMillis - startTime))
        context.system.shutdown()
      }
    case Setup =>
      println("Manager -               ---- START")
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
        val randIdx = random.nextInt(numNodes) + 1
        context.actorSelection(s"/user/node$randIdx") ! StartPushSum
      }
    case StartGossip =>
      if (!setupCompleted) {
        startTime = System.currentTimeMillis
        self ! StartGossip
      } else if (!completed) {
        val randIdx = random.nextInt(numNodes) + 1
        val randString = "hotgossip" //random.nextString(5)
        println("Gossip is: " + randString)
        context.actorSelection(s"/user/node$randIdx") ! StartGossip(randString)
      }
  }
}
