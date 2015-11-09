package Objects

import Objects.ObjectTypes.ListType.ListType

case class FriendList(
                       id:Integer,
                       owner:Profile,
                       profiles:Array[Profile],
                       list_type:ListType
                     )
