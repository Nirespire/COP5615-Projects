package Server.Actors

import Objects.User

class UserActor(var user: User) extends ProfileActor {
  def userReceive: Receive = {
    case newUser: User => user = newUser
    case _ =>
  }

  override def receive = userReceive orElse super.receive
}