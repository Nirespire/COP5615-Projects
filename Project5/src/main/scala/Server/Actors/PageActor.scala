package Server.Actors

import Objects._
import Objects.ObjectTypes._
import ObjectJsonSupport._
import Server.Messages.{DeleteSecureObjMsg, GetSecureObjMsg, PostSecureObjMsg}
import Utils.{Constants, Crypto, DebugInfo}
import spray.json._

class PageActor(var page: SecureObject, debugInfo: DebugInfo)
  extends ProfileActor(page.baseObj.id, debugInfo) {

  def baseObject = page.baseObj

  def pageReceive: Receive = {
    case PostSecureObjMsg(rc, nPage@SecureObject(_, from, _, _, _, _)) if baseObject.deleted => rc.complete(
      Crypto.constructSecureMessage(
        Constants.serverId,
        "Page Deleted!",
        Constants.userPublicKeys(from),
        Constants.serverPrivateKey
      )
    )

    case DeleteSecureObjMsg(rc, SecureRequest(from, _, _, _)) if baseObject.deleted => rc.complete(
      Crypto.constructSecureMessage(
        Constants.serverId,
        "Page Already Deleted!",
        Constants.userPublicKeys(from),
        Constants.serverPrivateKey
      )
    )
    case GetSecureObjMsg(rc, SecureRequest(from, _, _, _)) if baseObject.deleted => rc.complete(
      Crypto.constructSecureMessage(
        Constants.serverId,
        "Page Deleted!",
        Constants.userPublicKeys(from),
        Constants.serverPrivateKey
      )
    )
    case DeleteSecureObjMsg(rc, SecureRequest(from, to, id, _)) if id == ObjectType.page.id =>
      if (from == to) {
        baseObject.delete()
        rc.complete(
          Crypto.constructSecureMessage(
            Constants.serverId,
            "Page Deleted",
            Constants.userPublicKeys(from),
            Constants.serverPrivateKey
          )
        )
      } else {
        rc.complete(
          Crypto.constructSecureMessage(
            Constants.serverId,
            "Unauthorized Delete! Not Deleted!",
            Constants.userPublicKeys(from),
            Constants.serverPrivateKey
          )
        )
      }
    case GetSecureObjMsg(rc, SecureRequest(from, to, id, _)) if id == ObjectType.page.id => rc.complete(
      Crypto.constructSecureMessage(
        Constants.serverId,
        page.toJson.compactPrint,
        Constants.userPublicKeys(from),
        Constants.serverPrivateKey
      )
    )
    case PostSecureObjMsg(rc, nPage@SecureObject(_, from, to, id, _, _)) if id == ObjectType.page.id =>
      if (from == to) {
        page = nPage
        rc.complete(Crypto.constructSecureMessage(
          Constants.serverId,
          "Page Updated",
          Constants.userPublicKeys(from),
          Constants.serverPrivateKey
        ))
      } else {
        rc.complete(
          Crypto.constructSecureMessage(
            Constants.serverId,
            "Unauthorized Update! Not Updated!",
            Constants.userPublicKeys(from),
            Constants.serverPrivateKey
          )
        )
      }
  }

  override def receive = pageReceive orElse super.receive
}