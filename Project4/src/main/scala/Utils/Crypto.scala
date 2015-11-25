package Utils

import java.security._
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


object Crypto {
  def generateKeys(): KeyPair = {
    // EC = Elliptic curve
    val keyGen = KeyPairGenerator.getInstance("EC")
    val random = SecureRandom.getInstance("SHA1PRNG");
    keyGen.initialize(256, random)
    keyGen.generateKeyPair()
  }

  def encrypt(bytes: Array[Byte], secret: Key): Array[Byte] = {
    val encipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    encipher.init(Cipher.ENCRYPT_MODE, secret)
    encipher.doFinal(bytes)
  }

  def decrypt(bytes: Array[Byte], secret: Key): Array[Byte] = {
    val encipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    encipher.init(Cipher.DECRYPT_MODE, secret)
    encipher.doFinal(bytes)
  }


  def privateToString(pk:PrivateKey) : String = {
    val privateString = new StringBuffer()
    val privateKey = pk.getEncoded()
    for(i <- privateKey) {
      privateString.append(Integer.toHexString(0x0100 + (i & 0x00FF)).substring(1))
    }

    privateString.toString()
  }

  def publicToString(pk:PublicKey) : String = {
    val privateString = new StringBuffer()
    val privateKey = pk.getEncoded()
    for(i <- privateKey) {
      privateString.append(Integer.toHexString(0x0100 + (i & 0x00FF)).substring(1))
    }

    privateString.toString()
  }


  def buildKey(password: Array[Byte]) : Key = {
    val digester = MessageDigest.getInstance("SHA-256")
    digester.update(password)
    val key = digester.digest()
    new SecretKeySpec(key, "AES")
  }
}
