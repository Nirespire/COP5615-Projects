package Server.Actors

import Objects.Page
import akka.actor.ActorRef

class PageActor(var page: Page, debugActor: ActorRef) extends ProfileActor(debugActor) {
  def pageReceive: Receive = {
    case newPage: Page => page = newPage
  }

  override def receive = pageReceive orElse super.receive
}
