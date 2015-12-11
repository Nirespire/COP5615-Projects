package Objects

import Objects.ObjectTypes.PostType.PostType

case class Post(baseObject: BaseObject,
                creator: Long,
                createdTime: String,
                from: Long,
                message: String,
                postType: PostType,
                attachmentId: Long)