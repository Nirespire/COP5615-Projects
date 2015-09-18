import messages.Topology

object project2 extends App {

  val n = args(0).toInt
  val topology = args(1) match {
    case "3D" => Topology.threeD
    case _ => Topology.withName(args(1))
  }

  val  algorithm = args(1) match {
    case "push-sum" =>
  }

}
