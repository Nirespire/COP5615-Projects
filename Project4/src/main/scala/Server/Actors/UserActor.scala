package Server.Actors

import Objects.User
import Objects.ObjectJsonSupport._
import Server.Messages.GetUser
import spray.json._

class UserActor(var user: User) extends ProfileActor {
  def userReceive: Receive = {
    case newUser: User => user = newUser
    case GetUser(rc) => rc.complete(user.toJson.compactPrint)
    case _ =>
  }

  override def receive = userReceive orElse super.receive
}