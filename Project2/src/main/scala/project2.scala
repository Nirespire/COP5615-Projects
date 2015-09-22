import akka.actor.{ActorSystem, Props}
import messages.{Topology, Algorithm, Setup}

object project2 extends App {

  val arg0 = args(0).toInt

  // num nodes can be rounded to the nearest perfect square
  val (numNodes, topology) = args(1) match {
    case "2D" => (Math.pow(Math.ceil(Math.sqrt(arg0)), 2).toInt, Topology.twoD)
    case "3D" => (Math.pow(Math.ceil(Math.cbrt(arg0)), 3).toInt, Topology.threeD)
    case "imp2D" => (Math.pow(Math.ceil(Math.sqrt(arg0)), 2).toInt, Topology.imp2D)
    case "imp3D" => (Math.pow(Math.ceil(Math.cbrt(arg0)), 3).toInt, Topology.imp3D)
    case _ => (arg0, Topology.withName(args(1)))
  }

  val algorithmName = args(2)
  val algorithm = algorithmName match {
    case "push-sum" => Algorithm.pushSum
    case "gossip" => Algorithm.gossip
  }

  println("Num nodes " + numNodes)
  println("Topology " + topology)
  println("Algorithm " + algorithm)

  val system = ActorSystem(name = algorithmName)
  val manager = system.actorOf(Props(new Manager(numNodes, algorithm)), name = s"manager")

  (1 to numNodes).foreach { idx =>
    println(s"Creating $idx node")
    println(system.actorOf(Props(new Node(idx, topology, numNodes)), name = s"node$idx"))
  }

  manager ! Setup
}
