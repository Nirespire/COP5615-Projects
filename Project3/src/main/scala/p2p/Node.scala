package p2p

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import core.{CircularRing, FingerEntry, Message}

import scala.concurrent.Await
import scala.concurrent.duration._

class Node(m: Integer, n: Int, numRequests: Int) extends Actor {

  // Finger table to hold at most m entries
  implicit val timeout = Timeout(5 seconds)
  val predecessorIdx = 0
  val selfIdx = 1
  val successorIdx = 2
  val finger = new Array[FingerEntry](m + successorIdx)

  var numRequestsSent = 0
  val done = numRequestsSent == numRequests

  (successorIdx to m + 1).foreach { i =>
    finger(i) = FingerEntry(start = CircularRing.addI(n, i - successorIdx), node = n, nodeRef = self)
  }

  finger(predecessorIdx) = FingerEntry(start = CircularRing.subtractOne(n), node = n, nodeRef = self)
  finger(selfIdx) = FingerEntry(start = n, node = n, nodeRef = self)

  // Pointer to predecessor for quick access
  def predecessor = finger(predecessorIdx).nodeRef

  def predecessorId = finger(predecessorIdx).node

  def successor = finger(successorIdx).nodeRef

  def successorId = finger(successorIdx).node

  def closest_preceding_node(id: Int): Int = {
    (successorIdx to m + 1).reverse.foreach { i =>
      if (CircularRing.inBetweenWithoutStartWithoutStop(n, finger(i).node, id)) {
        return i
      }
    }
    return selfIdx
  }

  def predecessorLookup(id: Int): (Boolean, Int) = {
    if (!CircularRing.inBetweenWithoutStartWithStop(n, id, finger(successorIdx).node)) {
      (false, closest_preceding_node((id)))
    }
    else {
      (true, selfIdx)
    }
  }

  def updateFinderEntry(s: Int, i: Int) = {
    if (CircularRing.inBetweenWithStartWithoutStop(n, s, finger(i).node)) {
      if (n != s) {
        finger(i).update(s, sender)
        println("After finger pred found,  update o finger table - " + finger.mkString("-"))
      }
      if (predecessorId != s) {
        predecessor.forward(Message.UpdateFingerEntry(s, i))
      }
    }
  }


  def lookup(id: Int): (Boolean, Int) = {
    if (CircularRing.inBetweenWithoutStartWithStop(n, id, successorId)) {
      (true, successorIdx)
    } else {
      val nprime = closest_preceding_node(id)
      (false, nprime)
    }
  }

  def receive = {

    // initial node will have all its finger entries as itself
    case Message.InitialNode =>
      println("Node: initial node setting up")
      println("Finger Table:")
      println(finger.mkString("-"))

    case key: Int =>

    case knownNode: ActorRef =>
      //debug
      println("Node: setting up new node")
      // Find my successor
      knownNode ! Message.GetSuccessor(finger(successorIdx).start)

    case Message.UpdateFingerPredecessor(key, s, i) =>
      //debug
      println("Trying to find predecessor for " + key + " using existing node " + n + " to update " + s)

      val (lookupResult, lookupIdx) = lookup(key)
      println("for " + key + " at id " + n + " we found it at " + lookupIdx + " " + finger(lookupIdx))
      if (finger(lookupIdx).node != s) {
        if (lookupResult || n == finger(lookupIdx).node) {
          updateFinderEntry(s, i)
        }
        else if (n != finger(lookupIdx).node) {
          finger(lookupIdx).nodeRef.forward(Message.UpdateFingerPredecessor(key, s, i))
        }
      }

    // Find finger entry whose id lies between nodeId
    case Message.GetSuccessor(id) =>
      //debug
      println("Trying to find successor for new node: " + id + " using existing node " + n)

      val (lookupResult, lookupIdx) = lookup(id)

      if (lookupResult || n == finger(lookupIdx).node) {
        println("Got successor for " + id + " at " + finger(selfIdx))
        println("for " + id + " at node " + n + " we found it at " + lookupIdx + " " + finger(lookupIdx))

        sender.tell(Message.YourSuccessor(finger(lookupIdx).node, finger(selfIdx)), finger(lookupIdx).nodeRef)
      } else {
        println("Forwarding lookup to: " + finger(lookupIdx))
        finger(lookupIdx).nodeRef.forward(Message.GetSuccessor(id))
      }

    // Do something once you get your successor
    case Message.YourSuccessor(senderId, ft) =>
      // Set our predecessor based on the predecessor info sent by our successor
      finger(predecessorIdx).update(ft.node, ft.nodeRef, ft.node)

      // Set our successor
      finger(successorIdx).update(senderId, sender)
      println(finger.mkString("-"))

      // Tell our new successor to set us as their new predecessor
      sender ! Message.UpdatePredecessor(n)

      // Build our finger table
      (successorIdx to m).foreach { i =>
        val j = i + 1
        if (CircularRing.inBetweenWithStartWithoutStop(n, finger(j).start, finger(i).node)) {
          finger(j).update(finger(i).node, finger(i).nodeRef)
        } else {
          val future = sender ? Message.GetFingerSuccessor(j, finger(j).start)
          val result = Await.result(future, timeout.duration).asInstanceOf[Message.YourFingerSuccessor]
          //          (i, senderId)]
          finger(result.i).update(result.n, result.nRef)
        }
      }
      // Tell other nodes to update their fingers
      updateOthers()

    // Find finger entry whose id lies between nodeId
    case Message.GetFingerSuccessor(i, id) =>
      //debug
      println("Trying to find finger successor for: " + id + " using existing node " + n)

      val (lookupResult, lookupIdx) = lookup(id)
      println("for " + id + " at id " + n + " we found it at " + lookupIdx + " " + finger(lookupIdx))

      if (lookupResult || n == finger(lookupIdx).node) {
        sender ! Message.YourFingerSuccessor(nRef = finger(lookupIdx).nodeRef, n = finger(lookupIdx).node,
          i = i)
      } else {
        finger(lookupIdx).nodeRef.forward(Message.GetFingerSuccessor(i, id))
      }

    // Do something once you get your successor

    case Message.UpdatePredecessor(pid) => finger(predecessorIdx).update(pid, sender, pid)
      println("After pred update - " + finger.mkString("-"))
    /*
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
          if (CircularRing.inbetween(predecessorId, n, queryVal)) {
            manager ! Message.QueryMessage(queryVal, numHops)
          }
          // This node is not responsible, pass on the request
          else {
            val fingerIdx = lookup(queryVal)
            finger(fingerIdx).nodeRef ! Message.QueryMessage(queryVal, numHops + 1)
          }
          */

    case true => println("FINAL FINGER TABLE::::" + finger.mkString("-"))

  }

  def updateOthers() = {
    println("Node " + n + " is done. Update others: " + finger.mkString("-"))
    (successorIdx to m + 1).foreach { i =>
      val key = CircularRing.subtractI(n, i - successorIdx)
      self ! Message.UpdateFingerPredecessor(key, n, i)
    }
  }
}