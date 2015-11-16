package Objects

import Objects.ObjectTypes.ListType.ListType

case class FriendList(
                       var id: Int,
                       owner: Int,
                       profiles: Array[Int],
                       list_type: ListType
                     ){
  def updateId(newId:Int){
    id = newId
  }

}