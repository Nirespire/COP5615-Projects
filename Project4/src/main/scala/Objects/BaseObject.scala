package Objects

import scala.collection.mutable

case class BaseObject(var id: Int = -1) {

  val likes = mutable.Set[Int]()

  def updateId(newId: Int) = id = newId

  def appendLike(pid: Int) = {
    likes.add(pid)
  }
}