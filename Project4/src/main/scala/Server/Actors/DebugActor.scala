package Server.Actors

import Server.Messages._
import akka.actor.Actor
import spray.json._
import spray.routing.RequestContext
import Objects.ObjectJsonSupport._

class DebugActor extends Actor {

  var profiles: Int = 0
  var posts: Int = 0
  var albums: Int = 0
  var friendlists: Int = 0


  def receive = {

    case CreateProfile =>
      profiles += 1

    case CreatePost =>
      posts += 1

    case CreateAlbum =>
      albums += 1

    case CreateFriendList =>
      friendlists += 1

    case GetServerInfo(rc: RequestContext) =>
      rc.complete(DebugMessage(profiles,posts,albums,friendlists).toJson.compactPrint)

    case _ =>

  }
}
