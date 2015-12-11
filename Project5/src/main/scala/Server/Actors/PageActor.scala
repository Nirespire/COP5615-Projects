package Server.Actors

import Objects._
import Utils.{Constants, Crypto, DebugInfo}

class PageActor(var page: SecureObject, debugInfo: DebugInfo)
  extends ProfileActor(page.baseObj.id, debugInfo) {

  val deletedPage = Crypto.constructSecureMessage(
    page.baseObj.id,
    "Page Deleted", Constants.defaultPublicKey, Constants.defaultPrivateKey
  )

  def baseObject = page.baseObj

  def pageReceive: Receive = {
    case _ =>
  }

  override def receive = pageReceive orElse super.receive
}