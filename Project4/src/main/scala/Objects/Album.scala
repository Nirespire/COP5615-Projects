package Objects

import scala.collection.mutable

case class Album(baseObject: BaseObject,
                 from: Int,
                 var createdTime: String,
                 var updatedTime: String,
                 var coverPhoto: Int,
                 var description: String) {
  val pictures = mutable.Set[Int]()

  def addPicture(picId: Int) = pictures.add(picId)
}