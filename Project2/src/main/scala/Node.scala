import akka.actor.{ActorSelection, ActorRef, Actor}
import messages.{StartPushSum, Setup, Topology}

import scala.collection.mutable
import scala.util.Random

class Node(id: Int, topology: Topology.Value, numNodes: Int) extends Actor {
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

  def left(n:Int):Int={
    return n-1
  }

  def right(n:Int):Int={
    return n+1
  }
  def down(n:Int):Int={
    return n + Math.sqrt(numNodes).toInt
  }
  def up(n:Int):Int={
    return n - Math.sqrt(numNodes).toInt
  }

  def receive = {
    case Setup =>
      val neighborsSet = mutable.Set[Int]()
      topology match {
        case Topology.threeD =>
          // top left corner
          if(id == 1){
            neighborsSet.add(right(id))
            neighborsSet.add(down(id))
          }
          // bottom right corner
          else if(id == numNodes){
            neighborsSet.add(left(id))
            neighborsSet.add(up(id))
          }
          // top right corner
          else if(id == (numNodes/(Math.sqrt(numNodes))).toInt){
            neighborsSet.add(left(id))
            neighborsSet.add(down(id))
          }
          // bottom left corner
          else if(id == (numNodes - Math.sqrt(numNodes) + 1).toInt){
            neighborsSet.add(right(id))
            neighborsSet.add(up(id))
          }

          // top edge
          else if(id > 1 && id < numNodes/Math.sqrt(numNodes)){
            neighborsSet.add(left(id))
            neighborsSet.add(right(id))
            neighborsSet.add(down(id))
          }
          // bottom edge
          else if(id > numNodes - Math.sqrt(numNodes) + 1 && id < numNodes){
            neighborsSet.add(left(id))
            neighborsSet.add(right(id))
            neighborsSet.add(up(id))
          }
          // right edge
          else if(id % Math.sqrt(numNodes) == 0){
            neighborsSet.add(left(id))
            neighborsSet.add(up(id))
            neighborsSet.add(down(id))
          }
          // left edge
          else if(id % Math.sqrt(numNodes) == 1){
            neighborsSet.add(right(id))
            neighborsSet.add(up(id))
            neighborsSet.add(down(id))
          }
          // somewhere in the center
          else{
            neighborsSet.add(left(id))
            neighborsSet.add(right(id))
            neighborsSet.add(up(id))
            neighborsSet.add(down(id))
          }
          
        case Topology.line =>
          if (id - 1 > 0) neighborsSet.add(left(id))
          if (id + 1 <= numNodes) neighborsSet.add(right(id))
        case Topology.imp3D =>
        case Topology.full => (1 to numNodes).foreach(neighborsSet.add)
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