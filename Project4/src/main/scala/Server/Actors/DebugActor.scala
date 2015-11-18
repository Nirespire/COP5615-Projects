package Server.Actors

import Server.Messages._
import akka.actor.Actor
import spray.json._
import spray.routing.RequestContext
import Objects.ObjectJsonSupport._

class DebugActor(var profiles:Int = 0, var posts:Int = 0, var albums:Int = 0, var friendlistUpdates:Int = 0) extends Actor {


  def receive = {

    case CreateProfile =>
      profiles += 1

    case CreatePost =>
      posts += 1

    case CreateAlbum =>
      albums += 1

    case UpdateFriendList =>
      friendlistUpdates += 1

    case GetServerInfo(rc: RequestContext) =>
      rc.complete(this.toJson.compactPrint)

    case _ =>

  }
}
