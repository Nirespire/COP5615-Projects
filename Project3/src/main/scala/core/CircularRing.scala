package core

object CircularRing {
  var hashSpace: Int = _

  def setM(m: Int) = hashSpace = Math.pow(2, m).toInt

  def subtractOne(n: Int) = subtractI(n, 0)

  def subtractI(n: Int, i: Int) = (n + hashSpace - Math.pow(2, i).toInt) % hashSpace

  def addOne(n: Int) = addI(n, 0)

  def addI(n: Int, i: Int) = (n + Math.pow(2, i).toInt) % hashSpace

  def inBetweenWithoutStartWithoutStop(start: Int, ipId: Int, ipStop: Int) = {
    val stop = if (start >= ipStop) ipStop + hashSpace else ipStop
    val id = if (ipId <= ipStop && start >= ipStop) ipId + hashSpace else ipId
    start < id && id < stop
  }

  def inBetweenWithStartWithoutStop(start: Int, ipId: Int, ipStop: Int) = {
    val stop = if (start >= ipStop) ipStop + hashSpace else ipStop
    val id = if (ipId <= ipStop && start >= ipStop) ipId + hashSpace else ipId
    start <= id && id < stop
  }

  def inBetweenWithoutStartWithStop(start: Int, ipId: Int, ipStop: Int) = {
    val stop = if (start >= ipStop) ipStop + hashSpace else ipStop
    val id = if (ipId <= ipStop && start >= ipStop) ipId + hashSpace else ipId
    start < id && id <= stop
  }
}