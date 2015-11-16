package Server.Messages

import spray.routing.RequestContext

case class GetPost(requestContext: RequestContext, id: Int)
