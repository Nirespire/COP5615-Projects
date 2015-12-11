package Server.Messages

import javax.crypto.SecretKey

import spray.routing.RequestContext

case class PutMsg(
                   rc: RequestContext,
                   message: Array[Byte],
                   aesKey: SecretKey
                 )