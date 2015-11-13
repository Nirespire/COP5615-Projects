package Messages

import Objects.Post
import spray.routing.RequestContext

case class CreatePost(requestContext:RequestContext, post:Post)
