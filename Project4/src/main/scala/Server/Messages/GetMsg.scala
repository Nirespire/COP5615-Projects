package Server.Messages

import spray.routing.RequestContext

case class GetMsg[T](requestContext: RequestContext, pid: Int, params: T)