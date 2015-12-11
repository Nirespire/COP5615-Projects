package Objects

import Utils.Base64Util

case class SecureObject(
                         baseObj: BaseObject,
                         objectType: Int,
                         data: Array[Byte],
                         //                         encryptedKey: Map[Int, Array[Byte]]
                         encryptedKeys: Map[Int, Array[Byte]]
                       ) {
  override def toString(): String = {
    val sb = new StringBuilder()
    sb.append(s"Object belongs to: $baseObj, ObjectType: $objectType,\n")
    sb.append(s"Base64Content: ${Base64Util.encodeString(data)}, \n")
    sb.append(s"Encrypted Key: ${Base64Util.encodeString(encryptedKeys.mkString(","))}\n")
    sb.toString()
  }
}