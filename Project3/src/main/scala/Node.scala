import akka.actor.{Actor, ActorRef}

import scala.util.Random

class Node(manager: ActorRef, hashSpace: Int, m: Integer, id: Int, numRequests: Int) extends Actor {

  // Finger table to hold at most m entries
  val fingerTable = new Array[FingerEntry](m + 2)
  val predecessorIdx = 0
  val successorIdx = 2

  var numRequestsSent = 0
  val done = numRequestsSent == numRequests

  (2 to m + 1).foreach { i =>
    fingerTable(i) = FingerEntry(nodeId = (id + Math.pow(2, i - 2).toInt) % hashSpace, successorId = id, successor = self)
  }

  fingerTable(0) = FingerEntry(nodeId = (id + hashSpace - 1) % hashSpace, successorId = id, successor = self)
  fingerTable(1) = FingerEntry(nodeId = id, successorId = id, successor = self)

  // Pointer to predecessor for quick access
  def predecessor = fingerTable(predecessorIdx).successor

  def predecessorId = fingerTable(predecessorIdx).successorId

  def successor = fingerTable(successorIdx).successor

  def successorId = fingerTable(successorIdx).successorId

  def lookup(key: Int): Int = {
    (0 to m).foreach { idx =>
      val jIdx = idx + 1
      if (CircularRing.inbetween(fingerTable(idx).nodeId, key, fingerTable(jIdx).nodeId, hashSpace)) {
        return jIdx
      }
    }

    return m + 1
  }

  def receive = {

    // initial node will have all its finger entries as itself
    case Message.InitialNode => {
      println("Node: initial node setting up")
      println("Finger Table:")
      println(fingerTable.mkString("\n"))
      manager ! Message.Done
    }

    case key: Int => {
      //received a key to lookup
      val fingerIdx = lookup(key)

      if (fingerTable(fingerIdx).successorId == id) {
        sender ! id
      } else {
        sender ! fingerIdx
      }
    }

    // New node joining is given a ref to known node in the system
    // This is called by the new node
    case knownNode: ActorRef => {
      //debug
      println("Node: setting up new node")
      // Find my successor
      knownNode ! Message.GetNodeSuccessor(id)
    }

    // Find finger entry whose id lies between nodeId
    case Message.GetNodeSuccessor(nodeId) => {
      //debug
      println("Trying to find successor for new node: " + nodeId + " using existing node " + id)

      val fingerIdx = lookup(nodeId)

      println("for " + nodeId + " at id " + id + " we found it at " + fingerIdx + " " + fingerTable(fingerIdx))
      if (fingerTable(fingerIdx).successorId == id) {
        sender ! Message.YourSuccessor(id, fingerTable)
      } else {
        fingerTable(fingerIdx).successor.forward(Message.GetNodeSuccessor(nodeId))
      }
    }

    // Do something once you get your successor
    case Message.YourSuccessor(senderId, ft) => {
      fingerTable(0) = fingerTable(0).updateSuccessor(ft(0).successorId, ft(0).successorId, ft(0).successor)

      var jIdx = 2
      var kIdx = 3

      println(fingerTable(0) + "____" + ft(0))
      println(fingerTable(1) + "____" + ft(1))
      (2 to m + 1).foreach { idx =>
        if (CircularRing.inbetween(id, fingerTable(idx).nodeId, senderId, hashSpace)) {
          fingerTable(idx) = fingerTable(idx).updateSuccessor(senderId, sender)
        } else {
          //TODO:look at ft to update fingerTable.
          while (!CircularRing.
            inbetween(ft(jIdx).nodeId, fingerTable(idx).nodeId, ft(kIdx).nodeId, hashSpace) && kIdx < m - 1) {
            if (fingerTable(idx).nodeId <= ft(jIdx).successorId) {
              fingerTable(idx) = fingerTable(idx).
                updateSuccessor(ft(jIdx).successorId, ft(jIdx).successor)
            }
            jIdx += 1
            kIdx += 1
          }
        }
        println(fingerTable(idx) + "____" + ft(idx))
      }

      sender ! Message.UpdatePredecessor(id)
      predecessor ! Message.UpdateFingers(id, successorId)
      //      Thread.sleep(1000)
      manager ! Message.Done
      //Send message to update the new node in finger
    }

    case Message.UpdatePredecessor(pid) => fingerTable(0) = fingerTable(0).updateSuccessor(pid, pid, sender)

    case Message.UpdateFingers(pid, spid) => {
      if (pid != predecessorId && CircularRing.inbetween(predecessorId, pid, spid, hashSpace)) {
        predecessor ! Message.UpdateFingers(pid, spid)
      }

      (2 to m + 1).foreach { idx =>
        if (CircularRing.inbetween(fingerTable(idx).nodeId, pid, fingerTable(idx).successorId, hashSpace)) {
          fingerTable(idx) = fingerTable(idx).updateSuccessor(pid, sender)
        }
      }

      println(fingerTable.mkString("\n"))
    }

    case Message.SendQueryMessage =>{
      if(done){
        manager ! Message.Done
      }
      else {
        numRequestsSent += 1
        // generate random hash
        val query = Random.nextInt(hashSpace)
        // query system
        self ! Message.QueryMessage(query, 0)
      }
      // Send a query every 1 second
      Thread.sleep(1000)
      self ! Message.SendQueryMessage
    }

    case Message.QueryMessage(queryVal, numHops) => {
      // This node is responsible for the queryVal hash
      if(CircularRing.inbetween(predecessorId, id, queryVal, hashSpace)){
        manager ! Message.QueryMessage(queryVal, numHops)
      }
      // This node is not responsible, pass on the request
      else{
        val fingerIdx = lookup(queryVal)
        fingerTable(fingerIdx).successor ! Message.QueryMessage(queryVal, numHops + 1)
      }
    }

  }
}