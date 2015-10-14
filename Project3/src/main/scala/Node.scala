import akka.actor.{Actor, ActorRef}
import akka.pattern.ask

class Node(m: Integer, id: Int) extends Actor {
  // Finger table to hold at most m entries
  val fingerTable = new Array[Int](m + 2)

  def lookup(key: Int) = {

  }

  def receive = {
    case key: Int => {
      //received a key to lookup

    }
    case knownNode: ActorRef =>
  }
}