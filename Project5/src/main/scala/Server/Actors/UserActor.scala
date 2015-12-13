package Server.Actors

import Objects.ObjectTypes.ObjectType
import Objects._
import Objects.ObjectJsonSupport._
import Server.Messages.{LikeMsg, GetSecureObjMsg, DeleteSecureObjMsg, PostSecureObjMsg}
import Utils.{Constants, Crypto, DebugInfo}
import spray.json._
import spray.routing.RequestContext

class UserActor(var user: SecureObject, debugInfo: DebugInfo)
  extends ProfileActor(user.baseObj.id, debugInfo) {

  def baseObject = user.baseObj

  def userReceive: Receive = {
    case LikeMsg(rc, SecureRequest(from, _, _, _)) if baseObject.deleted => handleUserDeleted(rc, from)
    case PostSecureObjMsg(rc, nUser@SecureObject(_, from, _, _, _, _)) if baseObject.deleted =>
      handleUserDeleted(rc, from)
    case DeleteSecureObjMsg(rc, SecureRequest(from, _, _, _)) if baseObject.deleted => handleUserDeleted(rc, from)
    case GetSecureObjMsg(rc, SecureRequest(from, _, _, _)) if baseObject.deleted => handleUserDeleted(rc, from)
    case DeleteSecureObjMsg(rc, SecureRequest(from, to, oid, _)) if oid == ObjectType.user.id =>
      if (from == to) {
        baseObject.delete()
        handleUserDeleted(rc, from)
      } else {
        handleUnauthorizedRequest(rc, from)
      }
    case GetSecureObjMsg(rc, SecureRequest(from, to, oid, _)) if oid == ObjectType.user.id =>
      rc.complete(
        Crypto.constructSecureMessage(
          Constants.serverId,
          user.toJson.compactPrint,
          Constants.userPublicKeys(from),
          Constants.serverPrivateKey
        )
      )
    case PostSecureObjMsg(rc, nUser@SecureObject(_, from, to, oid, _, _)) if oid == ObjectType.user.id =>
      if (from == to) {
        user = nUser
        rc.complete(Crypto.constructSecureMessage(
          Constants.serverId,
          "User Updated",
          Constants.userPublicKeys(from),
          Constants.serverPrivateKey
        ))
      } else {
        handleUnauthorizedRequest(rc, from)
      }
  }

  override def receive = userReceive orElse super.receive

  def handleUserDeleted(rc: RequestContext, from: Int) = rc.complete(
    Crypto.constructSecureMessage(
      Constants.serverId,
      "User Deleted!",
      Constants.userPublicKeys(from),
      Constants.serverPrivateKey
    )
  )

  def handleUnauthorizedRequest(rc: RequestContext, from: Int) = rc.complete(
    Crypto.constructSecureMessage(
      Constants.serverId,
      "Unauthorized Request! Not Request!",
      Constants.userPublicKeys(from),
      Constants.serverPrivateKey
    )
  )
}
