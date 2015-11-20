package Server.Actors

import Objects.ObjectTypes.ListType.ListType
import Objects.{UpdateFriendList, User}
import Server.Messages.{UpdateMsg, ResponseMessage}
import akka.actor.ActorRef
import spray.routing.RequestContext
import Objects.ObjectJsonSupport._
import spray.json._

import scala.collection.mutable

class UserActor(var user: User, debugActor: ActorRef) extends ProfileActor(debugActor) {
  val friendsMap = mutable.Map[ListType, Set[Int]]()

  def userReceive: Receive = {
    case UpdateMsg(rc, newUser: User) =>
      user = newUser
      rc.complete(user)
    case rc: RequestContext => rc.complete(user)
    case UpdateMsg(rc, upd: UpdateFriendList) =>
      user.b.appendLike(upd.fid)
      if (friendsMap.contains(upd.listType)) {
        friendsMap(upd.listType) = friendsMap(upd.listType) + upd.fid
      } else {
        friendsMap(upd.listType) = Set(upd.fid)
      }

      rc.complete(upd)
  }

  override def receive = userReceive orElse super.receive
}