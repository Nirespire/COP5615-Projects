package Objects

import Objects.ObjectTypes.PostType.PostType

case class Post(b: BaseObject,
                creator: Int,
                createdTime: String,
                from: Int,
                message: String,
                postType: PostType)
