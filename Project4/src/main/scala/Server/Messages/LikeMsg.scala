package Server.Messages

import spray.routing.RequestContext

case class LikeMsg[T](rc: RequestContext, pid: Int,fid:Int, obj: T) {

}
