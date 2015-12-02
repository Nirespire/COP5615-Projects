package Utils

import java.security._
import javax.crypto.{KeyGenerator, Cipher}
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

object Crypto {
  def generateRSAKeys(): KeyPair = {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    val random = SecureRandom.getInstance("SHA1PRNG")
    keyGen.initialize(1024, random)
    keyGen.generateKeyPair()
  }

  def generateAESKey(): Key = {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(128)
    keyGen.generateKey()
  }

  def encryptRSA(bytes: Array[Byte], publicKey: Key): Array[Byte] = {
    val encipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    encipher.init(Cipher.ENCRYPT_MODE, publicKey)
    encipher.doFinal(bytes)
  }

  def decryptRSA(bytes: Array[Byte], privateKey: Key): Array[Byte] = {
    val encipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    encipher.init(Cipher.DECRYPT_MODE, privateKey)
    encipher.doFinal(bytes)
  }

  def encryptAES(bytes: Array[Byte], secret: Key, iv:Array[Byte]): Array[Byte] = {
    val encipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    val skeySpec = new SecretKeySpec(secret.getEncoded(), "AES")
    val ivSpec = new IvParameterSpec(iv)
    encipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec)
    encipher.doFinal(bytes)
  }

  def decryptAES(bytes: Array[Byte], secret: Key, iv:Array[Byte]): Array[Byte] = {
    val encipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    val skeySpec = new SecretKeySpec(secret.getEncoded(), "AES")
    val ivSpec = new IvParameterSpec(iv)
    encipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec)
    encipher.doFinal(bytes)
  }

  def byteArrayToHexString(bytes: Array[Byte]): String = {
    val keyString = new StringBuffer()
    for (i <- bytes) {
      keyString.append(Integer.toHexString(0x0100 + (i & 0x00FF)).substring(1))
    }
    keyString.toString()
  }

  def md5(bytes: Array[Byte]): Array[Byte] = {
    MessageDigest.getInstance("MD5").digest(bytes)
  }

  def sha256(bytes: Array[Byte]): Array[Byte] = {
    MessageDigest.getInstance("SHA-256").digest(bytes)
  }

  def sha512(bytes: Array[Byte]): Array[Byte] = {
    MessageDigest.getInstance("SHA-512").digest(bytes)
  }

}
