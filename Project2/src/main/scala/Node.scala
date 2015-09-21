import akka.actor.{ActorSelection, ActorRef, Actor}
import messages.{StartGossip, StartPushSum, Setup, Topology}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.util.Random

class Node(id: Int, topology: Topology.Value, numNodes: Int) extends Actor {
  val config = ConfigFactory.load()
  val rumor = mutable.HashMap[String, Int]().withDefaultValue(0)
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

  def gossipAlgo(newRumor: String){
    println(self + rumor.mkString(","))

    if(!done){
      val rumorUpdate = rumor(newRumor)+1
      rumor.update(newRumor, rumorUpdate)
      if(rumorUpdate == config.getInt("app.gossipConvergenceNum")){
        done = true
        manager ! true
      }
    }

    neighbors(random.nextInt(numOfNeighbors)) ! StartGossip(newRumor)
  }

  def appendNeighbors(neighborsSet: Set[Int]) = {
    neighborsSet.foreach { idx =>
      neighbors.append(context.actorSelection(s"/user/node$idx"))
    }
  }

  def west(n:Int):Int={
    return n-1
  }
  def east(n:Int):Int={
    return n+1
  }
  def south(n:Int):Int={
    return n + Math.sqrt(numNodes).toInt
  }
  def north(n:Int):Int={
    return n - Math.sqrt(numNodes).toInt
  }
  def up(n:Int):Int={
    return n - Math.pow(numNodes,2).toInt
  }
  def down(n:Int):Int={
    return n + Math.pow(numNodes,2).toInt
  }



  def setup3D {
    val neighborsSet = mutable.Set[Int]()

    // CORNERS
    //----------------------------------------
    // up top left corner
    if(id == 1){
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
      neighborsSet.add(down(id))
    }

    // up top right corner
    if(id == Math.cbrt(numNodes)){
      neighborsSet.add(west(id))
      neighborsSet.add(south(id))
      neighborsSet.add(down(id))
    }

    // up bottom left corner
    if(id == Math.pow(Math.cbrt(numNodes),2) - Math.cbrt(numNodes) + 1 ){
      neighborsSet.add(north(id))
      neighborsSet.add(east(id))
      neighborsSet.add(down(id))
    }

    // up bottom right corner
    if(id == Math.pow(Math.cbrt(numNodes),2)){
      neighborsSet.add(north(id))
      neighborsSet.add(west(id))
      neighborsSet.add(down(id))
    }

    // down top left corner
    if(id == numNodes - Math.pow(Math.cbrt(numNodes),2) + 1){
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
      neighborsSet.add(up(id))
    }

    // down top right corner
    if(id == numNodes - Math.pow(Math.cbrt(numNodes),2) + Math.cbrt(numNodes) - 1){
      neighborsSet.add(west(id))
      neighborsSet.add(south(id))
      neighborsSet.add(up(id))
    }

    // down bottom left corner
    if(id == numNodes - Math.cbrt(numNodes) + 1){
      neighborsSet.add(east(id))
      neighborsSet.add(north(id))
      neighborsSet.add(up(id))
    }

    // down bottom right corner
    if(id == numNodes){
      neighborsSet.add(west(id))
      neighborsSet.add(north(id))
      neighborsSet.add(up(id))
    }

    // TOP HORIZONTAL EDGES
    //----------------------------------------

    //up top edge
    if(id > 1 && id < Math.cbrt(numNodes)){
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
      neighborsSet.add(down(id))
    }

    // up right edge

    // up left edge

    // up bottom edge
    if(id > Math.pow(Math.cbrt(numNodes),2) - Math.cbrt(numNodes + 1) && id < Math.pow(Math.cbrt(numNodes),2)){
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(down(id))
      neighborsSet.add(north(id))
    }

    // VERTICAL EDGES
    //------------------------------------------

    // up to down top left edge
    if(id % (((numNodes)/Math.pow(Math.cbrt(numNodes),2)) - Math.pow(Math.cbrt(numNodes),2) + 1) == 0){
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
      neighborsSet.add(down(id))
      neighborsSet.add(up(id))
    }

    // up to down top right edge
    if(id % (numNodes)/Math.pow(Math.cbrt(numNodes),2) == 0){
      neighborsSet.add(west(id))
      neighborsSet.add(south(id))
      neighborsSet.add(down(id))
      neighborsSet.add(up(id))
    }

    // up to down bottom right edge
    if(id % Math.pow(Math.cbrt(numNodes),2) == 0){
      neighborsSet.add(north(id))
      neighborsSet.add(south(id))
      neighborsSet.add(down(id))
      neighborsSet.add(up(id))
    }

    // up to down bottom left edge
    if((id + Math.pow(Math.cbrt(numNodes),2) - 1) % Math.pow(Math.cbrt(numNodes),2) == 0){
      neighborsSet.add(north(id))
      neighborsSet.add(east(id))
      neighborsSet.add(down(id))
      neighborsSet.add(up(id))
    }

    // BOTTOM HORIZONTAL EDGES
    //----------------------------------------

    // down top edge
    if(id > numNodes - Math.pow(Math.cbrt(numNodes),2) + 1 && id < numNodes - Math.pow(Math.cbrt(numNodes),2) + Math.cbrt(numNodes) - 1){
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
      neighborsSet.add(up(id))
    }

    // down right edge

    // down bottom edge
    if(id > numNodes - Math.cbrt(numNodes) + 1 && id < numNodes){
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(north(id))
      neighborsSet.add(up(id))
    }

    // down left edge



  }

  def setup2D : mutable.Set[Int]={
    val neighborsSet = mutable.Set[Int]()
    // top left corner
    if(id == 1){
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
    }
    // bottom right corner
    else if(id == numNodes){
      neighborsSet.add(west(id))
      neighborsSet.add(north(id))
    }
    // top right corner
    else if(id == (numNodes/(Math.sqrt(numNodes))).toInt){
      neighborsSet.add(west(id))
      neighborsSet.add(south(id))
    }
    // bottom left corner
    else if(id == (numNodes - Math.sqrt(numNodes) + 1).toInt){
      neighborsSet.add(east(id))
      neighborsSet.add(north(id))
    }

    // top edge
    else if(id > 1 && id < numNodes/Math.sqrt(numNodes)){
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
    }
    // bottom edge
    else if(id > numNodes - Math.sqrt(numNodes) + 1 && id < numNodes){
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(north(id))
    }
    // right edge
    else if(id % Math.sqrt(numNodes) == 0){
      neighborsSet.add(west(id))
      neighborsSet.add(north(id))
      neighborsSet.add(south(id))
    }
    // left edge
    else if(id % Math.sqrt(numNodes) == 1){
      neighborsSet.add(east(id))
      neighborsSet.add(north(id))
      neighborsSet.add(south(id))
    }
    // somewhere in the center
    else{
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(north(id))
      neighborsSet.add(south(id))
    }

    return neighborsSet
  }

  def receive = {
    case Setup =>
      var neighborsSet = mutable.Set[Int]()
      topology match {
        case Topology.threeD =>
          neighborsSet = setup2D
          
        case Topology.line =>
          if (id - 1 > 0) neighborsSet.add(west(id))
          if (id + 1 <= numNodes) neighborsSet.add(east(id))
        case Topology.imp3D =>
        case Topology.full => (1 to numNodes).foreach(neighborsSet.add)
      }
      println(self + "^^^^^^^^^^^SETUP with neighbors " + neighborsSet.mkString(","))
      appendNeighbors(neighborsSet.toSet)
      manager = sender
      manager ! false
    case StartPushSum => pushSumAlgo(0, 0)
    case StartPushSum(addS: Double, addW: Double) => pushSumAlgo(addS, addW)
    case StartGossip(rumor: String) => gossipAlgo(rumor)
  }
}