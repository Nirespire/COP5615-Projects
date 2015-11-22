package Server.Actors

import Objects.Page
import Objects.ObjectJsonSupport._
import spray.json._
import Server.Messages.{UpdateMsg, GetMsg}
import akka.actor.ActorRef

class PageActor(var page: Page, debugActor: ActorRef)
  extends ProfileActor(page.baseObject.id, debugActor) {
  def pageReceive: Receive = {
    case updMsg@UpdateMsg(rc, _, newPage: Page) =>
      page = newPage
      rc.complete(page)

    case getMsg@GetMsg(rc, _, None) => rc.complete(page)
  }

  override def receive = pageReceive orElse super.receive
}
