package Server.Messages

import javax.crypto.SecretKey

import spray.routing.RequestContext

case class PutEncryptedMsg(
                            rc: RequestContext,
                            pid: Int,
                            message: Array[Byte],
                            aesKey: SecretKey
                          )