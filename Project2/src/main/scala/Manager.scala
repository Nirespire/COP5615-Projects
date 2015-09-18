import akka.actor.Actor
import messages.{StartGossip, Algorithm, Setup, StartPushSum}

import scala.util.Random

class Manager(n: Int, algorithm: Algorithm.Value) extends Actor {
  var completedCounter = 0
  var setupCounter = 0
  val random = new Random()

  def completed = completedCounter == n

  def setupCompleted = setupCounter == n

  def receive = {
    case false =>
      setupCounter += 1
      println(sender + "==================" + setupCounter)
    case true => completedCounter += 1
      println(sender + " ---- " + completedCounter)
      if (completed) {
        context.system.shutdown()
      }
    case Setup =>
      println("Manager -               ---- START")
      (1 to n).foreach { idx =>
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
        val randIdx = random.nextInt(n) + 1
        context.actorSelection(s"/user/node$randIdx") ! StartPushSum
      }
    case StartGossip =>
      if (!setupCompleted) {
        self ! StartGossip
      } else if (!completed) {
      }
  }
}
