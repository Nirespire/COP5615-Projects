package Objects

import scala.Array._

case class BaseObject(var id: Int = -1, val likes: Array[Int] = Array()) {

  def updateId(newId: Int) = id = newId

  def appendLikes(pids: Array[Int]) = concat(likes, pids)
}