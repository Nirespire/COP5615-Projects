package Objects

import Objects.ObjectTypes.ListType

case class UpdateFriendList(
                             pid: Long,
                             fid: Long,
                             listType: ListType.Value = ListType.friend
                           )
