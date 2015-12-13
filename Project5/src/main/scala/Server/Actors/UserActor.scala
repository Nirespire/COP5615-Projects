package Server.Actors

import Objects.ObjectTypes.ObjectType
import Objects._
import Objects.ObjectJsonSupport._
import Server.Messages.{GetSecureObjMsg, DeleteSecureObjMsg, PostSecureObjMsg}
import Utils.{Constants, Crypto, DebugInfo}
import spray.json._

class UserActor(var user: SecureObject, debugInfo: DebugInfo)
  extends ProfileActor(user.baseObj.id, debugInfo) {

  def baseObject = user.baseObj

  def userReceive: Receive = {
    case PostSecureObjMsg(rc, nUser@SecureObject(_, from, _, _, _, _)) if baseObject.deleted => rc.complete(
      Crypto.constructSecureMessage(
        Constants.serverId,
        "User Deleted!",
        Constants.userPublicKeys(from),
        Constants.serverPrivateKey
      )
    )
    case DeleteSecureObjMsg(rc, SecureRequest(from, _, _, _)) if baseObject.deleted => rc.complete(
      Crypto.constructSecureMessage(
        Constants.serverId,
        "User Already Deleted!",
        Constants.userPublicKeys(from),
        Constants.serverPrivateKey
      )
    )
    case GetSecureObjMsg(rc, SecureRequest(from, _, _, _)) if baseObject.deleted => rc.complete(
      Crypto.constructSecureMessage(
        Constants.serverId,
        "User Deleted!",
        Constants.userPublicKeys(from),
        Constants.serverPrivateKey
      )
    )
    case DeleteSecureObjMsg(rc, SecureRequest(from, to, oid, _)) if oid == ObjectType.user.id =>
      if (from == to) {
        baseObject.delete()
        rc.complete(
          Crypto.constructSecureMessage(
            Constants.serverId,
            "User Deleted",
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

  override def receive = userReceive orElse super.receive
}