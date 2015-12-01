import java.util

import Utils.Crypto
import org.scalatest.{Matchers, FlatSpec}

class CryptoSpec extends FlatSpec with Matchers {
  "The public key pair generator" should "generate 2 keys" in {

    val pair = Crypto.generateKeys()

    val privateKey = pair.getPrivate().getEncoded()
    val publicKey = pair.getPublic().getEncoded()

    val md5Private = Crypto.md5(privateKey)
    val md5Public = Crypto.md5(publicKey)

    val sha256Private = Crypto.sha256(privateKey)
    val sha256Public = Crypto.sha256(publicKey)

    println(Crypto.byteArrayToString(md5Private))
    md5Private.length * 8 should equal(128)
    println(Crypto.byteArrayToString(sha256Private))
    sha256Private.length * 8 should equal(256)

    println(Crypto.byteArrayToString(md5Public))
    md5Public.length * 8 should equal(128)
    println(Crypto.byteArrayToString(sha256Public))
    sha256Public.length * 8 should equal(256)


  }

  "Encryption Decryption Test" should "work" in {
    val pair = Crypto.generateKeys()
    val privateKey = pair.getPrivate()
    val publicKey = pair.getPublic

    println(Crypto.buildKey(privateKey.getEncoded()).getEncoded.length)
    val encrypted = Crypto.encrypt("Testing".getBytes(), Crypto.buildKey(privateKey.getEncoded()))

    //    println(util.Arrays.toString(encrypted))
    //
    //    val decrypted = Crypto.decrypt(encrypted, Crypto.buildKey(publicKey.getEncoded()))
    //
    //    println(util.Arrays.toString(decrypted))


  }


}
