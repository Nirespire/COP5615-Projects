package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.UpdFriendList
import Server.Messages._
import akka.actor.{ActorLogging, Actor, ActorRef, Props}

import spray.json._
import scala.collection.mutable

class DelegatorActor(debugActor: ActorRef) extends Actor {
  val profiles = mutable.ArrayBuffer[ActorRef]()

  def receive = {
    case CreateUser(requestContext, user) =>
      val start = System.nanoTime()
      try {
        user.b.updateId(profiles.size)
        profiles.append(context.actorOf(Props(new UserActor(user, debugActor))))
        requestContext.complete(user)
        debugActor ! CreateProfile
      } catch {
        case e: Throwable => requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
      println((System.nanoTime() - start) + " creation time")
    case x: CreatePost =>
      try {
        profiles(x.post.creator) ! x
      } catch {
        case e: Throwable => x.requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case x: CreateAlbum =>
      try {
        profiles(x.album.from) ! x
      } catch {
        case e: Throwable => x.requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case GetUser(requestContext, pid) =>
      try {
        profiles(pid) ! requestContext
      } catch {
        case e: Throwable => requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case x: UpdFriendList =>
      try {
        profiles(x.pid) ! x
      } catch {
        case e: Throwable => x.rc.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case _ =>
  }

}
