package Objects

case class Picture(baseObject: BaseObject,
                   from: Long,
                   var album: Long,
                   var filename: String,
                   var base64Encoded: String)
