package Objects

import Utils.{Constants, Crypto, Base64Util}


case class SecureObject[T](
                            baseObject: BaseObject,
                            base64Content: String,
                            encryptedKey: String
                          ) {

  override def toString(): String = {
    val sb = new StringBuilder()
    sb.append("BaseObject: ")
    sb.append(baseObject.toString())
    sb.append('\n')
    sb.append("Base64Content: ")
    sb.append(this.base64Content)
    sb.append('\n')
    sb.append("Encrypted Key: ")
    sb.append(this.encryptedKey)
    sb.toString()
  }
}

object SecureObjectUtil {

  import java.security.Key

  def constructSecureObject[T](baseObject: BaseObject, obj: Any, aesKey: Key, publicKey: Key): SecureObject[T] = {
    SecureObject[T](baseObject,
      Base64Util.encodeString(Crypto.encryptAES(Base64Util.objectToBytes(obj), aesKey, Constants.iv)),
      Base64Util.encodeString(Crypto.encryptRSA(aesKey.getEncoded, publicKey)))
  }
}
