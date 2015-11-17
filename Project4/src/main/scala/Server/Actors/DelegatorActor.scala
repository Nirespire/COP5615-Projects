package Server.Actors

import Objects.ObjectJsonSupport._
import Server.Messages._
import akka.actor.{ActorLogging, Actor, ActorRef, Props}

import spray.json._
import scala.collection.mutable

class DelegatorActor extends Actor with ActorLogging {
  val profiles = mutable.ArrayBuffer[ActorRef]()
  val debugActor = context.actorOf(Props(new DebugActor()))

  def receive = {
    case CreateUser(requestContext, user) =>
      debugActor ! CreateProfile
      try {
        user.b.updateId(profiles.size)
        profiles.append(context.actorOf(Props(new UserActor(user))))
        requestContext.complete(user)
      } catch {
        case e: Throwable => requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case x: CreatePost =>
      log.info(s"CreatePost  = ${x.post.creator}")
      debugActor ! CreatePost
      try {
        profiles(x.post.creator) ! x
      } catch {
        case e: Throwable => x.requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case x: CreateAlbum =>
      log.info(s"CreateAlbum = ${x.album.from}")
      debugActor ! CreateAlbum
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
    case x: GetServerInfo => debugActor ! x
    case _ =>
  }

}
