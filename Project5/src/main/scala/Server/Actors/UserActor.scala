package Server.Actors

import Objects._
import Utils.{Constants, Crypto, DebugInfo}

class UserActor(var user: SecureObject, debugInfo: DebugInfo)
  extends ProfileActor(user.baseObj.id, debugInfo) {

  val deletedUser = Crypto.constructSecureMessage(
    user.baseObj.id,
    "User Deleted", Constants.defaultPublicKey, Constants.defaultPrivateKey
  )

  def baseObject = user.baseObj

  def userReceive: Receive = {
    case _ =>
  }

  override def receive = /*userReceive orElse*/ super.receive
}