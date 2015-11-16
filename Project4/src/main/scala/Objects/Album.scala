package Objects

case class Album(b: BaseObject,
                 from: Int,
                 createdTime: String,
                 updatedTime: String,
                 coverPhoto: Int,
                 description: String,
                 pictures: Array[Int]
                )
