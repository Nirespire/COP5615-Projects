package Server.Actors

import Objects.User
import akka.actor.Actor

class UserActor(user: User) extends Actor {
  def receive = {
    case _ =>
  }
}
