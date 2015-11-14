package Server.Messages

import Objects.Post
import spray.routing.RequestContext

case class UpdatePost(requestContext:RequestContext, post: Post)
