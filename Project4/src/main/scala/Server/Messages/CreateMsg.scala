package Server.Messages

import spray.routing.RequestContext

case class CreateMsg[T](rc: RequestContext, pid: Int, obj: T)
