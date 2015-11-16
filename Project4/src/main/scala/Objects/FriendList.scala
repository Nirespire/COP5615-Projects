package Objects

import Objects.ObjectTypes.ListType.ListType

case class FriendList(
                       id: Int,
                       owner: Int,
                       profiles: Array[Int],
                       list_type: ListType
                     )