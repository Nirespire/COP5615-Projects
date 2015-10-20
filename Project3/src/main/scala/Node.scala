import akka.actor.{Actor, ActorRef}

import scala.util.Random

class Node(manager: ActorRef, hashSpace: Int, m: Integer, n: Int, numRequests: Int) extends Actor {

  // Finger table to hold at most m entries
  var updatedFingers = 0
  val predecessorIdx = 0
  val selfIdx = 1
  val successorIdx = 2
  val finger = new Array[FingerEntry](m + successorIdx)

  var numRequestsSent = 0
  val done = numRequestsSent == numRequests

  (successorIdx to m + 1).foreach { i =>
    finger(i) = FingerEntry(start = (n + Math.pow(2, i - successorIdx).toInt) % hashSpace, node = n, nodeRef = self)
  }

  finger(predecessorIdx) = FingerEntry(start = (n + hashSpace - 1) % hashSpace, node = n, nodeRef = self)
  finger(selfIdx) = FingerEntry(start = n, node = n, nodeRef = self)

  // Pointer to predecessor for quick access
  def predecessor = finger(predecessorIdx).nodeRef

  def predecessorId = finger(predecessorIdx).node

  def successor = finger(successorIdx).nodeRef

  def successorId = finger(successorIdx).node


  def lookup(id: Int): Int = {
    (predecessorIdx to m).foreach { idx =>
      val jIdx = idx + 1
      if (CircularRing.inbetween(finger(idx).start, key, finger(jIdx).start, hashSpace)) {
        return jIdx
      }
    }

    return m + 1
  }

  def receive = {

    // initial node will have all its finger entries as itself
    case Message.InitialNode =>
      println("Node: initial node setting up")
      println("Finger Table:")
      println(finger.mkString("-"))
      manager ! Message.Done

    case key: Int =>
      //received a key to lookup
      val fingerIdx = lookup(key)

      if (finger(fingerIdx).node == n) {
        sender ! n
      } else {
        sender ! fingerIdx
      }

    // New node joining is given a ref to known node in the system
    // This is called by the new node
    case knownNode: ActorRef =>
      //debug
      println("Node: setting up new node")
      // Find my successor
      knownNode ! Message.GetSuccessor(n)

    // Find finger entry whose id lies between nodeId
    case Message.GetSuccessor(nodeId) =>
      //debug
      println("Trying to find successor for new node: " + nodeId + " using existing node " + n)

      val fingerIdx = lookup(nodeId)

      println("for " + nodeId + " at id " + n + " we found it at " + fingerIdx + " " + finger(fingerIdx))
      if (finger(fingerIdx).node == n) {
        sender ! Message.YourSuccessor(n, finger(predecessorIdx))
      } else {
        finger(fingerIdx).nodeRef.forward(Message.GetSuccessor(nodeId))
      }

    // Do something once you get your successor
    case Message.YourSuccessor(senderId, ft) =>
      finger(predecessorIdx) = finger(predecessorIdx).updateSuccessor(ft.node, ft.node, ft.nodeRef)
      finger(successorIdx) = finger(successorIdx).updateSuccessor(senderId, sender)
      updatedFingers += 1

      println(n + "+++++++++++++++++" + finger(predecessorIdx))
      println(n + "+++++++++++++++++" + finger(selfIdx))
      println(n + "+++++++++++++++++" + finger(successorIdx))
      sender ! Message.UpdatePredecessor(n)

      (successorIdx to m).foreach { idx =>
        val jIdx = idx + 1
        if (CircularRing.inbetween(n, finger(jIdx).start, finger(idx).node, hashSpace)) {
          finger(jIdx) = finger(jIdx).updateSuccessor(finger(idx).node, finger(idx).nodeRef)
          println(n + "+++++++++++++++++" + finger(jIdx))
          updatedFingers += 1
          updateOthers
        } else {
          sender ! Message.GetFingerSuccessor(jIdx, finger(jIdx).start)
        }
      }


    // Find finger entry whose id lies between nodeId
    case Message.GetFingerSuccessor(idx, nodeId) =>
      //debug
      println("Trying to find successor for new node: " + nodeId + " using existing node " + n)

      val fingerIdx = lookup(nodeId)

      println("for " + nodeId + " at id " + n + " we found it at " + fingerIdx + " " + finger(fingerIdx))
      if (finger(fingerIdx).node == n) {
        sender ! Message.YourFingerSuccessor(idx, n)
      } else {
        finger(fingerIdx).nodeRef.forward(Message.GetFingerSuccessor(idx, nodeId))
      }

    // Do something once you get your successor
    case Message.YourFingerSuccessor(idx, senderId) =>
      updatedFingers += 1
      finger(idx) = finger(idx).updateSuccessor(senderId, sender)
      println(n + "+++++++++++++++++" + finger(idx))
      updateOthers

    case Message.UpdatePredecessor(pid) =>
      finger(predecessorIdx) = finger(predecessorIdx).updateSuccessor(pid, pid, sender)
      println("After pred update - " + finger.mkString("-"))

    case Message.UpdateFingers(pid, i) =>
      val str = "on node " + n + " Before Update (" + finger(i) + ") "
      if (CircularRing.inbetween(n, pid, finger(i).node, hashSpace) && pid != n) {
        finger(i) = finger(i).updateSuccessor(pid, sender)
        predecessor.forward(Message.UpdateFingers(pid, i))
      }

      println(str + "Using node _ " + pid + " _ Updating finger : " + i + " to " + finger(i))

    case Message.SendQueryMessage =>
      if (done) {
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

    case Message.QueryMessage(queryVal, numHops) =>
      // This node is responsible for the queryVal hash
      if (CircularRing.inbetween(predecessorId, n, queryVal, hashSpace)) {
        manager ! Message.QueryMessage(queryVal, numHops)
      }
      // This node is not responsible, pass on the request
      else {
        val fingerIdx = lookup(queryVal)
        finger(fingerIdx).nodeRef ! Message.QueryMessage(queryVal, numHops + 1)
      }

    case Message.LookUpForward(key, idx, i) =>
      val fingerIdx = lookup(key)
      if (finger(fingerIdx).node != n) {
        finger(fingerIdx).nodeRef.forward(Message.ForwardToPredecessor(n, i))
      }

    case Message.ForwardToPredecessor(idx, i) => predecessor.forward(Message.UpdateFingers(idx, i))
  }

  def updateOthers() = {
    //TODO:Last thing to fix.
    if (updatedFingers == m) {
      (successorIdx to m + 1).foreach { i =>
        val key = Math.abs(n - Math.pow(2, i - successorIdx).toInt) % hashSpace
        self ! Message.LookUpForward(key, n, i)
      }

      manager ! Message.Done
    }
  }
}