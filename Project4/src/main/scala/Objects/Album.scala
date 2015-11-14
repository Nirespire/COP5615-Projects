package Objects


import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.DateTime
import spray.httpx.SprayJsonSupport
import spray.json._


case class Album (
                   id:Int,
                   from:Int,
                   createdTime:String,
                   updatedTime:String,
                   coverPhoto:Int,
                   description:String,
                   pictures:Array[Int]
                 )

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



