package Objects

case class SecureMessage(
                          from: Long,
                          message: Array[Byte],
                          signature: Array[Byte],
                          encryptedKey: Array[Byte]
                        )