import akka.actor.{ActorSelection, ActorRef, Actor}
import messages.{StartGossip, StartPushSum, Setup, Topology, SetupGossip, InitialGossip}
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
  val (planeSize, rowSize) = topology match {
    case Topology.threeD | Topology.imp3D => (Math.pow(Math.cbrt(numNodes), 2).toInt, Math.cbrt(numNodes).toInt)
    case _ => (numNodes, Math.sqrt(numNodes).toInt)
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
    //debug
    //    println(self + "-_- (" + s + "/" + w + ") = " + sOverWCalc + " conver" + convergenceCounter)
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

  def gossipAlgo(newRumor: String) {
    //debug
//        println(self + rumor.mkString(","))

    if (!done) {
      val convergenceNum = config.getInt("app.gossipConvergenceNum")
      val rumorUpdate = rumor(newRumor) + 1
      if (rumorUpdate <= convergenceNum) {
        rumor.update(newRumor, rumorUpdate)
      }
      if (rumor.values.forall(i => i == convergenceNum)) {
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

  def west(n: Int): Int = {
    if (id % rowSize == 1) {
      -1
    } else {
      n - 1
    }
  }

  def east(n: Int): Int = {
    if (id % rowSize == 0) {
      -1
    } else {
      n + 1
    }
  }

  def south(n: Int): Int = {
    val newId = id % planeSize
    if ((newId >= planeSize - rowSize + 1 && newId <= planeSize) || newId == 0) {
      -1
    } else {
      n + rowSize
    }
  }

  def north(n: Int): Int = {
    val newId = id % planeSize
    if (newId >= 1 && newId <= rowSize) {
      -1
    } else {
      n - rowSize
    }
  }

  // 3D cases
  def up(n: Int): Int = {
    n - planeSize
  }

  def down(n: Int): Int = {
    n + planeSize
  }


  def setup3D: mutable.Set[Int] = {
    val neighborsSet = setup2D

    if (id > 0 && id <= planeSize) {
      // top plane
      neighborsSet.add(down(id))
    } else if (id > planeSize * (cubeRoot - 1) && id <= numNodes) {
      // bottom plane
      neighborsSet.add(up(id))
    } else {
      // else, somewhere in the middle
      neighborsSet.add(down(id))
      neighborsSet.add(up(id))
    }

    neighborsSet
  }

  def setup2D: mutable.Set[Int] = {
    val neighborsSet = mutable.Set[Int]()
    val eastVal = east(id)
    val westVal = west(id)
    val northVal = north(id)
    val southVal = south(id)

    if (eastVal != -1) {
      neighborsSet.add(eastVal)
    }

    if (westVal != -1) {
      neighborsSet.add(westVal)
    }

    if (northVal != -1) {
      neighborsSet.add(northVal)
    }

    if (southVal != -1) {
      neighborsSet.add(southVal)
    }

    neighborsSet
  }

  def receive = {
    case Setup =>
      var neighborsSet = mutable.Set[Int]()
      topology match {
        case Topology.threeD => neighborsSet = setup3D
        case Topology.twoD => neighborsSet = setup2D
        case Topology.line =>
          if (id - 1 > 0) neighborsSet.add(west(id))
          if (id + 1 <= numNodes) neighborsSet.add(east(id))
        case Topology.imp2D =>
          neighborsSet = setup2D
          var randomNeighbor = random.nextInt(numNodes) + 1
          while (randomNeighbor == id || neighborsSet.contains(randomNeighbor)) {
            randomNeighbor = random.nextInt(numNodes) + 1
          }
          neighborsSet.add(randomNeighbor)
        case Topology.imp3D =>
          neighborsSet = setup3D
          var randomNeighbor = random.nextInt(numNodes) + 1
          while (randomNeighbor == id || neighborsSet.contains(randomNeighbor)) {
            randomNeighbor = random.nextInt(numNodes) + 1
          }
          neighborsSet.add(randomNeighbor)
        case Topology.full => (1 to numNodes).foreach(neighborsSet.add)
      }
      // debug
      //      println(self + "^^^^^^^^^^^SETUP with neighbors " + neighborsSet.mkString(","))
      appendNeighbors(neighborsSet.toSet)
      manager = sender()
      manager ! false
    case StartPushSum => pushSumAlgo(0, 0)
    case StartPushSum(addS: Double, addW: Double) => pushSumAlgo(addS, addW)
    case SetupGossip(initialRumor: String) =>
      val rumorUpdate = rumor(initialRumor) + 1
      rumor.update(initialRumor, rumorUpdate)
    case InitialGossip =>
      //debug
      //println(rumor.mkString(","))
      rumor.keySet.foreach(i => neighbors(random.nextInt(numOfNeighbors)) ! StartGossip(i))
    case StartGossip(rumor: String) => gossipAlgo(rumor)
  }
}