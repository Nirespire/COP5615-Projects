package Server.Actors

import Objects.ObjectTypes.ListType.ListType
import Objects.{UpdateFriendList, User}
import Server.Messages.{GetMsg, UpdateMsg, ResponseMessage}
import akka.actor.ActorRef
import spray.routing.RequestContext
import Objects.ObjectJsonSupport._
import spray.json._

import scala.collection.mutable

class UserActor(var user: User, debugActor: ActorRef)
  extends ProfileActor(user.baseObject.id, debugActor) {
  val friendsMap = mutable.Map[ListType, Set[Int]]()

  def userReceive: Receive = {
    case updMsg@UpdateMsg(rc, _, newUser: User) =>
      user = newUser
      rc.complete(user)
    case updMsg@UpdateMsg(rc, _, upd: UpdateFriendList) =>
      user.baseObject.appendLike(upd.fid)
      if (friendsMap.contains(upd.listType)) {
        friendsMap(upd.listType) = friendsMap(upd.listType) + upd.fid
      } else {
        friendsMap(upd.listType) = Set(upd.fid)
      }
      rc.complete(upd)
    case getMsg@GetMsg(rc, _, ("user", -1)) => rc.complete(user)
    case getMsg@GetMsg(rc, _, listType: ListType) =>
      rc.complete(JsArray(friendsMap.getOrElse(listType, Set()).map(f => JsNumber(f)).toVector))
  }

  override def receive = userReceive orElse super.receive
}