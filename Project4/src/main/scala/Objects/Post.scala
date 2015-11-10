package Objects

import java.util.Date

import Objects.ObjectTypes.PostType
import Objects.ObjectTypes.PostType.PostType
import Objects.ObjectTypes.PostType.PostType
import spray.json.{JsonFormat, DefaultJsonProtocol}

case class Post (
                  id:Integer,
                  creator:Profile,
                  createdTime:Date,
                  from:Profile,
                  message:String,
                  postType: PostType
                ) extends Profile