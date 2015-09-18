import akka.actor.{ActorSystem, Props}
import messages.{Algorithm, Setup, Topology}

object project2 extends App {

  val n = args(0).toInt
  val topology = args(1) match {
    case "3D" => Topology.threeD
    case _ => Topology.withName(args(1))
  }

  val algorithmName = args(2)
  val algorithm = algorithmName match {
    case "push-sum" => Algorithm.pushSum
    case "gossip" => Algorithm.gossip
  }

  val system = ActorSystem(name = algorithmName)
  val manager = system.actorOf(Props(new Manager(n, algorithm)), name = s"manager")

  (1 to n).foreach { idx =>
    println(s"Creating $idx node")
    println(system.actorOf(Props(new Node(idx, topology, n)), name = s"node$idx"))
  }

  manager ! Setup
}
