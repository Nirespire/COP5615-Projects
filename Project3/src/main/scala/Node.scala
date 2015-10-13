import java.nio.charset.Charset
import akka.actor.{ActorSelection, ActorRef, Actor}
import com.google.common.hash.Hashing
import scala.collection.mutable

class Node(knownNode: ActorRef = None, m: Integer = 0, id:Int) extends Actor {
  // Finger table to hold at most m entries
  val fingerTable = new Array[FingerEntry](m + 2)
  // HashMap of actual values
  val values = new mutable.HashMap[String, String]()
  val numKeys = values.size

  //  def successor: ActorRef = fingerTable(m + 1).successor

  //  def predecessor: ActorRef = fingerTable(0).successor

  // Function to join the Chord ring
  def join(actorRef: ActorRef) {

  }


  def lookup(key: String) {


  }

  // need this to output a number, not a string?
  def hash(value: String, m: Integer): Int = {
    Hashing.consistentHash(Hashing.sha256().hashString(value, Charset.forName("UTF-8")), m)
  }

  def receive = {
    case "" => {

    }


  }
}