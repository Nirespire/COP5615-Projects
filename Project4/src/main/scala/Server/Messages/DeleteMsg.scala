package Server.Messages

import spray.routing.RequestContext

case class DeleteMsg[T](rc: RequestContext, pid:Int, obj:T)
