package Server.Messages

import spray.routing.RequestContext

case class GetMsg[T](rc: RequestContext, pid: Int, params: T)