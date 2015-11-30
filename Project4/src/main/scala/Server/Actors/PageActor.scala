package Server.Actors

import Objects.Page
import Objects.ObjectJsonSupport._
import spray.json._
import Server.Messages._
import akka.actor.ActorRef

class PageActor(var page: Page, debugActor: ActorRef)
  extends ProfileActor(page.baseObject.id, debugActor) {

  def baseObject = page.baseObject

  def pageReceive: Receive = {
    case cMsg@CreateMsg(rc, _, p: Page) =>
      if (baseObject.deleted) {
        rc.complete(ResponseMessage("Page already deleted!"))
      } else {
        page = p
        rc.complete(page)
      }
    case updMsg@UpdateMsg(rc, _, newPage: Page) =>
      if (baseObject.deleted) {
        rc.complete(ResponseMessage("Page already deleted!"))
      } else {
        page = newPage
        rc.complete(page)
      }
    case getMsg@GetMsg(rc, _, ("page", -1)) =>
      if (baseObject.deleted) {
        rc.complete(ResponseMessage("Page already deleted!"))
      } else {
        rc.complete(page)
      }
    case deleteMsg@DeleteMsg(rc, _, None) => baseObject.delete(rc, s"Page ${page.baseObject.id}")
    case likeMsg@LikeMsg(rc, _, fid, ("page", _)) =>
      if (baseObject.deleted) {
        rc.complete(ResponseMessage(s"Profile $pid already deleted"))
      } else {
        baseObject.appendLike(fid)
        rc.complete(ResponseMessage("Page liked"))
      }
  }

  override def receive = pageReceive orElse super.receive
}