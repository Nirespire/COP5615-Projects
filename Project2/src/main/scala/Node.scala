import akka.actor.{ActorSelection, ActorRef, Actor}
import messages.{StartPushSum, Setup, Topology}

import scala.collection.mutable
import scala.util.Random

class Node(id: Int, topology: Topology.Value, n: Int) extends Actor {
  val rumor = mutable.HashMap[String, Int]()
  val random = new Random()
  val tenDigitConst = Math.pow(10, 10)
  val pushSum = Array[Double](id, 1)

  var sOverW: Double = sOverWCalc
  var convergenceCounter = 0
  var neighbors = mutable.ArrayBuffer[ActorSelection]()
  var done = false
  var manager: ActorRef = _

  def s: Double = pushSum(0)

  def w: Double = pushSum(1)

  def numOfNeighbors = neighbors.length

  def sOverWCalc = Math.round(s / w * tenDigitConst) / tenDigitConst

  def pushSumAlgo(newS: Double, newW: Double) = {
    println(self + "-_- (" + s + "/" + w + ") = " + sOverWCalc + " conver" + convergenceCounter)
    if (!done) {
      pushSum(0) = (s + newS) / 2
      pushSum(1) = (w + newW) / 2
      if (sOverW == sOverWCalc) {
        convergenceCounter += 1
        if (convergenceCounter == 3) {
          done = true
          manager ! true
        }
      } else {
        sOverW = sOverWCalc
        convergenceCounter = 0
      }

      neighbors(random.nextInt(numOfNeighbors)) ! StartPushSum(s, w)
    } else {
      neighbors(random.nextInt(numOfNeighbors)) ! StartPushSum(newS, newW)
    }
  }

  def appendNeighbors(neighborsSet: Set[Int]) = {
    neighborsSet.foreach { idx =>
      neighbors.append(context.actorSelection(s"/user/node$idx"))
    }
  }

  def receive = {
    case Setup =>
      val neighborsSet = mutable.Set[Int]()
      topology match {
        case Topology.threeD =>
        case Topology.line =>
          if (id - 1 > 0) neighborsSet.add(id - 1)
          if (id + 1 <= n) neighborsSet.add(id + 1)
        case Topology.imp3D =>
        case Topology.full => (1 to n).foreach(neighborsSet.add)
      }
      println(self + "^^^^^^^^^^^SETUP with neighbors " + neighborsSet.mkString(","))
      appendNeighbors(neighborsSet.toSet)
      manager = sender
      manager ! false
    case StartPushSum => pushSumAlgo(0, 0)
    case StartPushSum(addS: Double, addW: Double) => pushSumAlgo(addS, addW)
    case rumor: String =>
    /* gossip algo */
  }
}