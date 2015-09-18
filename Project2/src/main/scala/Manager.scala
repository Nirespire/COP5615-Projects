import akka.actor.Actor
import messages.{Continue, Algorithm, Setup, Start}

import scala.util.Random

class Manager(n: Int, algorithm: Algorithm.Value) extends Actor {
  var completedCounter = 0
  var setupCounter = 0
  val random = new Random()

  def completed = completedCounter == n

  def setupCompleted = setupCounter == n

  def receive = {
    case false => setupCounter += 1
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
        case Algorithm.pushSum => self ! Start
        case Algorithm.gossip => self ! Continue
      }
    case Start =>
      if (!setupCompleted) self ! Start
      val randIdx = random.nextInt(n) + 1
      context.actorSelection(s"/user/node$randIdx") ! Start
    case Continue =>
      if (!setupCompleted) self ! Continue
      if (!completed) {
      }
  }
}
