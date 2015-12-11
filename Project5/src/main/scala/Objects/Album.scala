package Objects

import scala.collection.mutable

case class Album(baseObject: BaseObject,
                 from: Long,
                 var createdTime: String,
                 var updatedTime: String,
                 var coverPhoto: Long,
                 var description: String) {
  val pictures = mutable.Set[Long]()

  def addPicture(picId: Long) = pictures.add(picId)
}