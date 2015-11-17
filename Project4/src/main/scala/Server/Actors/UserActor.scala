package Server.Actors

import Objects.ObjectTypes.ListType.ListType
import Objects.User
import spray.routing.RequestContext
import Objects.ObjectJsonSupport._
import spray.json._

import scala.collection.mutable

class UserActor(user: User) extends ProfileActor {
  val friendsMap = mutable.Map[ListType, mutable.Set[Int]]()

  def userReceive: Receive = {
    case rc: RequestContext => rc.complete(user)
    case _ =>
  }

  override def receive = userReceive orElse super.receive
}