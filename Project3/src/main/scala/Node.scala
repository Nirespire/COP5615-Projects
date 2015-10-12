import java.nio.charset.Charset
import akka.actor.{ActorRef, Actor}
import com.google.common.hash.Hashing
import scala.collection.mutable

class Node(m: Integer, successor: ActorRef, predecessor: ActorRef) extends Actor {
  // Finger table to hold at most m entries
  val fingerTable = new Array[FingerEntry](m)
  // HashMap of actual values
  val values = new mutable.HashMap[String,String]()
  val numKeys = values.size

  // Function to join the Chord ring
  def join(actorRef: ActorRef){

  }


  def lookup(key : String){


  }

  // need this to output a number, not a string?
  def hash(value: String, m: Integer): String ={
    Hashing.consistentHash(Hashing.sha256().hashString(value, Charset.forName("UTF-8")), m).toString;
  }


  def receive = {
    case "" => {

    }


  }
}