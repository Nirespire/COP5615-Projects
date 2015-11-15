package Objects


case class Album(
                  var id: Int,
                  from: Int,
                  createdTime: String,
                  updatedTime: String,
                  coverPhoto: Int,
                  description: String,
                  pictures: Array[Int]
                ) {
  def updateId(newId: Int) = id = newId
}

//object DateJsonFormat extends RootJsonFormat[DateTime] {
//
//  private val parserISO : DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis()
//
//  override def write(obj: DateTime) = JsString(parserISO.print(obj.getMillis))
//
//  override def read(json: JsValue) : DateTime = json match {
//    case JsString(s) => parserISO.parseDateTime(s)
//    case _ => throw new DeserializationException("Error info you want here ...")
//  }
//}

//object AlbumJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
//  implicit val AlbumJsonFormat = jsonFormat7(Album)
//}



