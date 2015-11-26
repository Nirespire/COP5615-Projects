package Objects

import Objects.ObjectTypes.ListType

case class UpdateFriendList(pid: Int,
                            fid: Int,
                            listType: ListType.Value = ListType.friend)
