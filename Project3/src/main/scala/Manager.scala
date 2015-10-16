import akka.actor.{Props, Actor, ActorRef}

import scala.collection.mutable
import scala.util.Random

class Manager(m: Integer) extends Actor {
  val createdNodes = mutable.ArrayBuffer[ActorRef]()
  var numNodesDone = 0

  def receive = {
    case nodeHash: Int => {

      // First node in the ring
      val newNode = if (createdNodes.isEmpty) {

        //debug
        println("Manager: creating initial node")

        val n = context.actorOf(Props(new Node(m = m, id = nodeHash)), name = s"Node$nodeHash")
        n ! Message.InitialNode
        n
      }
      // New node joining the ring
      else {

        //debug
        println("Manager: creating node")

        val knownNodeIdx = Random.nextInt(createdNodes.size)
        val knownNode = createdNodes(knownNodeIdx)
        val newNode = context.actorOf(Props(new Node(m = m, id = nodeHash)), name = s"Node$nodeHash")
        newNode ! knownNode
        newNode
      }

      createdNodes.append(newNode)
    }

    case Message.Done =>{
      numNodesDone = numNodesDone + 1
      if(numNodesDone == createdNodes.size){
        context.system.shutdown()
      }
    }

  }
}
