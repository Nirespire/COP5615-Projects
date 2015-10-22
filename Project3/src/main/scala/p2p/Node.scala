package p2p

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import core.{CircularRing, FingerEntry, Message}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

class Node(m: Integer, n: Int, numRequests: Int, manager: ActorRef) extends Actor {

  // Finger table to hold at most m entries
  implicit val timeout = Timeout(5 seconds)
  val predecessorIdx = 0
  val selfIdx = 1
  val successorIdx = 2
  val finger = new Array[FingerEntry](m + successorIdx)

  var numRequestsSent = 0
  var totalHops = 0

  def done = numRequestsSent == numRequests

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

  def updateFingerEntry(s: Int, i: Int) = {
    println(s"at $n with update $s for index $i(${finger(i).node} ")
    if (CircularRing.inBetweenWithStartWithoutStop(n, s, finger(i).node)) {
      if (n != s) {
        finger(i).update(s, sender)
        println(s"After finger pred found,  update finger table  $i - " + finger.mkString("-"))
      }
      predecessor.forward(Message.UpdateFingerEntry(s, i))
    }
  }


  def find_predecessor(id: Int): (Boolean, Int) = {
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
    case Message.QueryMessage(queryVal, numHops) =>
      val (result, fingerIdx) = find_predecessor(queryVal)

      if (result) {
        println("Found " + queryVal + " at " + successorId)
        sender ! Message.DoneQueryMessage(numHops + 1)
      }
      else {
        finger(fingerIdx).nodeRef forward Message.QueryMessage(queryVal, numHops + 1)
      }

    // Nodes have finished setting up, start querying every second
    case Message.StartQuerying => {
      if (!done) {
        self ! Message.QueryMessage(Random.nextInt(CircularRing.hashSpace), 0)
        context.system.scheduler.scheduleOnce(1.second, self, Message.StartQuerying)
      }
      else {
        manager ! Message.QueryingDone(n, totalHops / numRequestsSent)
      }
    }

    // Our query was fulfilled
    case Message.DoneQueryMessage(numHops) => {
      numRequestsSent += 1
      totalHops += numHops
    }

    case knownNode: ActorRef =>
      //debug
      println("Node: setting up new node")
      // Find my successor
      knownNode ! Message.GetSuccessor(finger(successorIdx).start)

    case Message.UpdateFingerPredecessor(key, s, i) =>
      //debug
      println("Trying to find predecessor for " + key + " using existing node " + n + " to update " + s)

      val (lookupResult, lookupIdx) = find_predecessor(key)
      println(s"for $key($i) at id $n we found it at $lookupIdx ${finger(lookupIdx)}")
      if (lookupResult) {
        updateFingerEntry(s, i)
      } else if (n == finger(lookupIdx).node) {
        updateFingerEntry(s, i)
      } else {
        finger(lookupIdx).nodeRef.forward(Message.UpdateFingerPredecessor(key, s, i))
      }

    case Message.UpdateFingerEntry(s, i) => updateFingerEntry(s, i)

    // Find finger entry whose id lies between nodeId
    case Message.GetSuccessor(id) =>
      //debug
      println("Trying to find successor for new node: " + id + " using existing node " + n)

      val (lookupResult, lookupIdx) = find_predecessor(id)

      if (lookupResult) {
        println("Got successor for " + id + " at " + finger(selfIdx))
        println("for " + id + " at node " + n + " we found it at " + lookupIdx + " " + finger(lookupIdx))
        sender.tell(Message.YourSuccessor(finger(lookupIdx).node, finger(selfIdx)), finger(lookupIdx).nodeRef)
      } else if (n == finger(lookupIdx).node) {
        sender ! Message.YourSuccessor(n, finger(predecessorIdx))
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
        } else if (CircularRing.inBetweenWithStartWithoutStop(predecessorId, finger(j).start, n)) {
          finger(j).update(predecessorId, predecessor)
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

      val (lookupResult, lookupIdx) = find_predecessor(id)
      println("for " + id + " at id " + n + " we found it at " + lookupIdx + " " + finger(lookupIdx))

      if (lookupResult) {
        sender ! Message.YourFingerSuccessor(nRef = successor, n = successorId, i = i)
      } else if (n == finger(lookupIdx).node) {
        sender ! Message.YourFingerSuccessor(nRef = self, n = n, i = i)
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

    case true => println("FINAL FINGER TABLE ::: " + finger.mkString("-"))
  }

  def updateOthers() = {
    println("Node " + n + " is done. Update others: " + finger.mkString("-"))
    (successorIdx to m + 1).foreach { i =>
      val key = CircularRing.subtractI(n, i - successorIdx)
      self ! Message.UpdateFingerPredecessor(key, n, i)
    }
  }
}