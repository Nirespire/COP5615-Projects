package Objects

import Objects.ObjectTypes.ListType
import spray.routing.RequestContext

case class UpdFriendList(pid: Int,
                         fid: Int,
                         listType: ListType.Value = ListType.friend) {
  var rc: RequestContext = _

  def updateRC(newrc: RequestContext) = rc = newrc
}