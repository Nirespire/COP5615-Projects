import akka.actor.{Actor, ActorRef}
import scala.collection.mutable

class Node(m: Integer, id: Int) extends Actor {
  val maxM = Math.pow(2,m.toDouble).toInt
  // Finger table to hold at most m entries
  var fingerTable = new Array[FingerEntry](m + 2)
  // Hold values at this node
  var keyValues = mutable.Map[Integer,String]();

  def lookup(key: Int) = {

  }

  def receive = {

    // initial node will have all its finger entries as itself
    case Message.InitialNode => {
      (0 to fingerTable.length - 1).foreach { i =>
        fingerTable(i) = new FingerEntry(node = self, nodeId = Math.pow(id, i).toInt, successorId = id, successor=self)
      }
    }


    case key: Int => {
      //received a key to lookup

    }

    // New node joining is given a ref to known node in the system
    case knownNode: ActorRef => {
      knownNode ! Message.GetNodeSuccessor(node=self, nodeId=id)
    }

      // http://www.cs.nyu.edu/courses/fall07/G22.2631-001/Chord.ppt
      /*
      // ask node n to find the successor of id
      n.find_successor(id)
        if (id elementOf (n, successor])
          return successor;
        else
          n' = closest_preceding_node(id);
          return n'.find_successor(id);

      // search the local table for the highest predecessor of id
      n.closest_preceding_node(id)
        for i = m downto 1
          if (finger[i] elementOf (n, id))
            return finger[i];
        return n;

       */

    // Find finger entry whose id lies between nodeId
    case Message.GetNodeSuccessor(node, nodeId) => {

      // If the node lies within the range of this node's successor
      if(CircularRing.inbetween(this.id,nodeId,fingerTable(0).nodeId, maxM)){
        sender ! Message.YourSuccessor(fingerTable(0).node, fingerTable(0).nodeId)
      }
      // Else search through finger table for closest preceding entry
      else{
        var closestPrecedingNode:ActorRef = null

        (fingerTable.length-1 to 1 by -1).foreach{ i=>
          if(CircularRing.inbetween(this.id,fingerTable(i).nodeId,nodeId,maxM)){
            closestPrecedingNode = fingerTable(i).node
          }
        }

        if(closestPrecedingNode == null) closestPrecedingNode = self

        closestPrecedingNode ! Message.GetNodeSuccessor(node, nodeId)
      }


      fingerTable.foreach{entry =>
        if(CircularRing.inbetween(entry.nodeId,nodeId,entry.successorId,maxM)){

        }
      }
    }

    // Do something once you get your successor
    case Message.YourSuccessor(node, nodeId) => {

    }

  }
}