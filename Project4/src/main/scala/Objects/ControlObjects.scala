package Objects

import Objects.ObjectTypes.PostType

object ControlObjects {
  val emptyPost = Post(BaseObject(0, true), -1, "", -1, "Invalid Post", PostType.empty, -1)
  val deletedPost = Post(BaseObject(1, true), -1, "", -1, "Post Deleted", PostType.empty, -1)

}
