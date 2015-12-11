package Objects

import Objects.ObjectTypes.PostType.PostType

case class Post(baseObject: BaseObject,
                creator: Int,
                createdTime: String,
                from: Int,
                message: String,
                postType: PostType,
                attachmentId: Int)