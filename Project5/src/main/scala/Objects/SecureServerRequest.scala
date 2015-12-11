package Objects

/**
  * Created by preethu on 12/11/15.
  */
case class SecureServerRequest(
                                from: Long,
                                signature: Array[Byte],
                                message: Array[Byte],
                                encryptedKey: Array[Byte]
                              )
