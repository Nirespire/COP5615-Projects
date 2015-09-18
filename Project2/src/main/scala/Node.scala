import akka.actor.{ActorRef, Actor}
import messages.{Start, Setup, Topology}

import scala.collection.mutable
import scala.util.Random

class Node(id: Int, topology: Topology.Value, n: Int) extends Actor {
  val rumor = mutable.HashMap[String, Int]()
  val random = new Random()
  val tenDigitConst = Math.pow(10, 10)
  val pushSum = Array[Double](id, 1)

  var sOverW: Double = sOverWCalc
  var convergenceCounter = 0
  var neighbors: Vector[ActorRef] = _
  var done = false

  def s: Double = pushSum(0)

  def w: Double = pushSum(1)

  def numOfNeighbors = neighbors.length

  def sOverWCalc = Math.round(s / w * tenDigitConst) / tenDigitConst

  def pushSumAlgo(newS: Double = 0, newW: Double = 0) = {
    if (!done) {
      pushSum(0) = (s + newS) / 2
      pushSum(1) = (w + newW) / 2
      if (sOverW == sOverWCalc) {
        convergenceCounter += 1
        if (convergenceCounter == 3) done = true
      } else {
        sOverW = sOverWCalc
        convergenceCounter = 0
      }
      neighbors(random.nextInt(numOfNeighbors)) !(s, w)
    }
  }

  def receive = {
    case Setup => topology match {
      case Topology.threeD =>
      case Topology.line =>
      case Topology.imp3D =>
      case Topology.full =>
    }

    case Start => /* push-sum algo */ pushSumAlgo()
    case (addS: Double, addW: Double) => pushSumAlgo(addS, addW)
    case rumor: String =>
    /* gossip algo */
  }
}