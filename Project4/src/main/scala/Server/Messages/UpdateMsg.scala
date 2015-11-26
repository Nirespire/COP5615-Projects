package Server.Messages

import spray.routing.RequestContext

case class UpdateMsg[T](rc: RequestContext, pid: Int, obj: T)
