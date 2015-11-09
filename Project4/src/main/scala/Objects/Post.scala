package Objects

import java.util.Date

import Objects.ObjectTypes.PostType.PostType

case class Post (
                  id:Integer,
                  creator:Profile,
                  createdTime:Date,
                  from:Profile,
                  message:String,
                  postType: PostType
                ) extends Profile