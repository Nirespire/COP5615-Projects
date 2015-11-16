package Server.Actors

import Objects.Page

class PageActor(var page: Page) extends ProfileActor {
  def pageReceive: Receive = {
    case newPage: Page => page = newPage
    case _ =>
  }

  override def receive = pageReceive orElse super.receive
}
