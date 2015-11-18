package Objects

case class Album(b: BaseObject,
                 from: Int,
                 var createdTime: String,
                 var updatedTime: String,
                 var coverPhoto: Int,
                 var description: String,
                 var pictures: Array[Int])