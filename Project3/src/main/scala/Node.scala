import akka.actor.{Actor, ActorRef}
import scala.collection.mutable

class Node(manager: ActorRef, m: Integer, id: Int) extends Actor {
  val maxM = Math.pow(2, m.toDouble).toInt

  // Finger table to hold at most m entries
  val fingerTable = new Array[FingerEntry](m + 1)
  val predecessorId = m

  // Hold values at this node
  val keyValues = mutable.Map[Integer,String]()

  (0 to m).foreach { i =>
    fingerTable(i) = new FingerEntry(nodeId = (id + Math.pow(2, i).toInt) % maxM, successorId = id, successor = self)
  }

  // Pointer to predecessor for quick access
  def predecessor = fingerTable(m).successor

  def successor = (0 until m).foldLeft(fingerTable(0)) { case (successor, idx) =>
    if (successor.successorId == id && fingerTable(idx).successorId != id) {
      fingerTable(idx)
    } else {
      successor
    }
  }.successor

  def lookup(key: Int) = {
    /*
    fingerTable.foreach { entry =>
      if (entry.nodeId > key) {
        entry
      }
    }
    */
    //TODO: dummy code, need to think out lookup
    fingerTable(0)
  }


  /*
  /// Taken from Chord paper
  def findSuccessor(key: Int) {
    var (predId, pred) = findPredecessor(key)
    // need to somehow get pred.successor
  }

  def findPredecessor(key: Int): (Int, ActorRef) = {
    var predId = (id, self)
    while (!CircularRing.inbetween2(id, key, fingerTable(0).successorId)) {
      predId = closestPrecedingFinger(key)
    }
    predId
  }

  def closestPrecedingFinger(key: Int): (Int, ActorRef) = {
    var output = (id, self)
    (fingerTable.length - 1 to 1 by -1).foreach { i =>
      if (CircularRing.inbetween2(this.id, key, fingerTable(i).nodeId)) {
        output = (fingerTable(i).successorId, fingerTable(0).successor)
      }
    }
    output
  }

  ///
  */
  def receive = {

    // initial node will have all its finger entries as itself
    case Message.InitialNode => {
      println("Node: initial node setting up")
      println("Finger Table:")
      print(fingerTable.mkString(","))
      manager ! Message.Done
    }


    case key: Int => {
      //received a key to lookup
    }

    // New node joining is given a ref to known node in the system
    // This is called by the new node
    case knownNode: ActorRef => {
      //debug
      println("Node: setting up new node")
      // Find my successor
      knownNode ! Message.GetNodeSuccessor(self, id)
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
      //debug
      println("Trying to find successor for new node: " + nodeId)

      val fingerEntry = lookup(nodeId)

      if (fingerEntry.successorId == id) {
        sender ! Message.YourSuccessor(fingerTable)
      } else {
        fingerEntry.successor ! sender.forward(Message.GetNodeSuccessor(node, nodeId))
      }

      /*
      // Second node entering the system
      //      if(fingerTable(0).nodeId == this.id){
      //        node ! Message.YourSuccessor(fingerTable(0).node, fingerTable(0).nodeId)
      //      }
      println(this.id)
      println(nodeId)
      println(fingerTable.size)
      println(fingerTable(0))

      // If the node lies within the range of this node's successor
      if (CircularRing.inbetween2(this.id, nodeId, fingerTable(0).successorId)) {
        //debug
        println("First successor case")
        node ! Message.YourSuccessor(fingerTable)

        // Need to modify self's finger table

      }
      // Else search through finger table for closest preceding entry
      else {
        //debug
        println("Second successor case")
        var closestPrecedingNode: ActorRef = null

        (fingerTable.length - 1 to 1 by -1).foreach { i =>
          if (CircularRing.inbetween2(this.id, nodeId, fingerTable(i).successorId)) {
            closestPrecedingNode = fingerTable(i).successor
          }
        }

        if (closestPrecedingNode == null) closestPrecedingNode = self

        closestPrecedingNode ! Message.GetNodeSuccessor(node, nodeId)
      }

    */
    }

    // Do something once you get your successor
    case Message.YourSuccessor(ft) => {
      //fingerTable.update(0, ft(0))
      //loop through ft while updating fingerTable
      //      fingerTable = ft
      println("Got my successor: " + ft(0))
      manager ! Message.Done
    }

  }
}