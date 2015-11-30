package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.ListType
import Objects.ObjectTypes.ListType
import Objects.{BaseObject, UpdateFriendList, User}
import Server.Messages._
import Utils.Constants
import akka.actor.ActorRef
import spray.json._

import scala.collection.mutable

class UserActor(var user: User, debugActor: ActorRef)
  extends ProfileActor(user.baseObject.id, debugActor) {

  val friendsMap = mutable.Map[ListType.Value, Set[Int]]()

  val deletedUser = User(BaseObject(deletedIdx, Constants.trueBool), "deleted", "00-00-0000", 'M', "deleted", "deleted")

  def baseObject = user.baseObject

  def userReceive: Receive = {
    case cMsg@CreateMsg(rc, _, u: User) =>
      if (baseObject.deleted) {
        rc.complete(deletedUser)
      } else {
        user = u
        rc.complete(user)
      }
    case updMsg@UpdateMsg(rc, _, newUser: User) =>
      if (baseObject.deleted) {
        rc.complete(deletedUser)
      } else {
        user = newUser
        rc.complete(user)
      }
    case updMsg@UpdateMsg(rc, _, upd: UpdateFriendList) =>
      if (baseObject.deleted) {
        rc.complete(deletedUser)
      } else {
        user.baseObject.appendLike(upd.fid)
        if (friendsMap.contains(upd.listType)) {
          friendsMap(upd.listType) = friendsMap(upd.listType) + upd.fid
        } else {
          friendsMap(upd.listType) = Set(upd.fid)
        }
        rc.complete(upd)
      }
    case getMsg@GetMsg(rc, _, ("user", -1)) =>
      if (baseObject.deleted) {
        rc.complete(deletedUser)
      } else {
        rc.complete(user)
      }
    case getMsg@GetMsg(rc, _, ("friendlist", listTypeIdx: Int)) =>
      if (baseObject.deleted) {
        rc.complete(JsArray())
//        rc.complete(deletedUser)
      } else {
        val listType = ListType(listTypeIdx)
        rc.complete(JsArray(friendsMap.getOrElse(listType, Set()).map(f => JsNumber(f)).toVector))
      }
    case deleteMsg@DeleteMsg(rc, _, None) =>
      user.baseObject.delete(rc, s"User $user")
  }

  override def receive = userReceive orElse super.receive
}