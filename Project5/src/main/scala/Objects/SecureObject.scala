package Objects

import Utils.Base64Util

case class SecureObject(
                         baseObj: BaseObject,
                         objectType: String,
                         data: Array[Byte],
                         encryptedKey: Array[Byte]
                       ) {
  override def toString(): String = {
    val sb = new StringBuilder()
    sb.append(s"Object belongs to: $baseObj, ObjectType: $objectType,\n")
    sb.append(s"Base64Content: ${Base64Util.encodeString(data)}, \n")
    sb.append(s"Encrypted Key: ${Base64Util.encodeString(encryptedKey)}\n")
    sb.toString()
  }
}