package Objects

case class Picture(baseObject: BaseObject,
                   from: Int,
                   var album: Int,
                   var filename: String,
                   var base64Encoded: String)
