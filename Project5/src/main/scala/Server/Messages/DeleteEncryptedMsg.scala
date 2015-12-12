package Server.Messages

import javax.crypto.SecretKey

import spray.routing.RequestContext

case class DeleteEncryptedMsg(
                            rc: RequestContext,
                            pid: Int,
                            message: Array[Byte],
                            aesKey: SecretKey
                          )