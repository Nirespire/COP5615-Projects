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

  val squareRoot = Math.sqrt(numNodes)
  val cubeRoot = Math.cbrt(numNodes)
  val (planeSize,rowSize) = topology match{
    case Topology.threeD | Topology.imp3D => (Math.pow(Math.cbrt(numNodes), 2).toInt, Math.cbrt(numNodes))
    case _ => (numNodes, Math.sqrt(numNodes))
  } 

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
    if(id % rowSize == 1){
      return -1
    }
    else{
      return n-1
    }
  }

  def east(n:Int):Int={
    if(id % rowSize == 0){
      -1
    }
    else{
      n+1
    }
  }

  def south(n:Int):Int={
    val newId = id % planeSize
    if((newId >= planeSize - rowSize + 1 && newId <= planeSize) || newId == 0){
      -1
    }
    else{
      n + Math.sqrt(planeSize).toInt
    }
  }

  def north(n:Int):Int={
    val newId = id % planeSize
    if(newId >= 1 && newId <= rowSize){
      -1
    }
    else{
      n - Math.sqrt(planeSize).toInt
    }
  }

  // 3D cases
  def up(n:Int):Int={
    return n - planeSize
  }
  def down(n:Int):Int={
    return n + planeSize
  }



  def setup3D : mutable.Set[Int] = {
    val neighborsSet = setup2D(true)

    // top plane
    if(id > 0 && id <= planeSize){
      neighborsSet.add(down(id))
    }

    // bottom plane
    else if(id > planeSize * (cubeRoot - 1) && id <= numNodes){
      neighborsSet.add(up(id))
    }

    // else, somewhere in the middle
    else{
      neighborsSet.add(down(id))
      neighborsSet.add(up(id))
    }

    return neighborsSet

  }

  def setup2D(for3D : Boolean = false) : mutable.Set[Int]={
    val neighborsSet = mutable.Set[Int]()

    val newId = if (for3D) id % planeSize else id

    println("ID " + id + " newID " + newId)

    val eastVal = east(id)
    val westVal = west(id)
    val northVal = north(id)
    val southVal = south(id)

    if(eastVal != -1){
      neighborsSet.add(eastVal)
    }
    if(westVal != -1){
      neighborsSet.add(westVal)
    }
    if(northVal != -1){
      neighborsSet.add(northVal)
    }
    if(southVal != -1){
      neighborsSet.add(southVal)
    }

/*
    // top left corner
    if(newId == 1){
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
    }
    // bottom right corner
    else if(newId == numNodes){
      neighborsSet.add(west(id))
      neighborsSet.add(north(id))
    }
    // top right corner
    else if(newId == (numNodes/(Math.sqrt(numNodes))).toInt){
      neighborsSet.add(west(id))
      neighborsSet.add(south(id))
    }
    // bottom left corner
    else if(newId == (numNodes - Math.sqrt(numNodes) + 1).toInt){
      neighborsSet.add(east(id))
      neighborsSet.add(north(id))
    }

    // top edge
    else if(newId > 1 && newId < numNodes/Math.sqrt(numNodes)){
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(south(id))
    }
    // bottom edge
    else if(newId > numNodes - Math.sqrt(numNodes) + 1 && newId < numNodes){
      neighborsSet.add(west(id))
      neighborsSet.add(east(id))
      neighborsSet.add(north(id))
    }
    // right edge
    else if(newId % Math.sqrt(numNodes) == 0){
      neighborsSet.add(west(id))
      neighborsSet.add(north(id))
      neighborsSet.add(south(id))
    }
    // left edge
    else if(newId % Math.sqrt(numNodes) == 1){
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
*/
    return neighborsSet
  }

  def receive = {
    case Setup =>
      var neighborsSet = mutable.Set[Int]()
      topology match {
        case Topology.threeD =>
          neighborsSet = setup3D

        case Topology.twoD =>
          neighborsSet = setup2D()
          
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