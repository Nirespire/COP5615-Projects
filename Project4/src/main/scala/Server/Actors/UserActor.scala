package Server.Actors

import Objects.{Post, User}
import akka.actor.Actor

import scala.collection.mutable

class UserActor(user: User) extends ProfileActor {
  profileId = user.id
}
