package Server.Messages

import spray.routing.RequestContext

case class DeletePost(requestContext: RequestContext, id: Int)
