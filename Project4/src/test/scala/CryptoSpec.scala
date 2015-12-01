import Utils.Crypto
import org.scalatest.{Matchers, FlatSpec}

class CryptoSpec extends FlatSpec with Matchers {
  "The public key pair generator" should "generate 2 keys" in {

    val pair = Crypto.generateRSAKeys()

    val privateKey = pair.getPrivate().getEncoded()
    val publicKey = pair.getPublic().getEncoded()

    val md5Private = Crypto.md5(privateKey)
    val md5Public = Crypto.md5(publicKey)

    val sha256Private = Crypto.sha256(privateKey)
    val sha256Public = Crypto.sha256(publicKey)

    println("MD5(privateKey)  " + Crypto.byteArrayToString(md5Private))
    md5Private.length * 8 should equal(128)
    println("SHA256(privateKey)  " + Crypto.byteArrayToString(sha256Private))
    sha256Private.length * 8 should equal(256)

    println("MD5(publicKey)  " + Crypto.byteArrayToString(md5Public))
    md5Public.length * 8 should equal(128)
    println("SHA256(publicKey)  " + Crypto.byteArrayToString(sha256Public))
    sha256Public.length * 8 should equal(256)
  }

  "RSA Encryption Decryption Test" should "work" in {
    val pair = Crypto.generateRSAKeys()
    val privateKey = pair.getPrivate()
    val publicKey = pair.getPublic
    val secretMessage = "You found my secret!"

    val encrypted = Crypto.encryptRSA(secretMessage.getBytes(), privateKey)

    println("Encrypted output:  " + Crypto.byteArrayToString(encrypted))

    val decrypted = Crypto.decryptRSA(encrypted, publicKey)
    val result = new String(decrypted)

    println("Decrypted output:  "  + result)
    result should equal(secretMessage)
  }

  "AES Encryption Decryption Test" should "work" in {
    val key = Crypto.generateAESKey()
    val secretMessage = "You found my secret!"

    val iv = Array[Byte]( 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 )

    val encrypted = Crypto.encryptAES(secretMessage.getBytes(), key, iv)

    println("Encrypted output:  " + Crypto.byteArrayToString(encrypted))

    val decrypted = Crypto.decryptAES(encrypted, key, iv)
    val result = new String(decrypted)

    println("Decrypted output:  "  + result)
    result should equal(secretMessage)
  }


}
