import java.util

import Utils.Crypto
import org.scalatest.{Matchers, FlatSpec}

class CryptoSpec extends FlatSpec with Matchers {
  "The public key pair generator" should "generate 2 keys" in {
    val pair = Crypto.generateKeys()

    val privateKey = pair.getPrivate().getEncoded()
    val publicKey = pair.getPublic().getEncoded()

    val privateString = new StringBuffer()
    val publicString = new StringBuffer()
    for(i <- privateKey) {
      privateString.append(Integer.toHexString(0x0100 + (i & 0x00FF)).substring(1))
    }

    for(i <- publicKey) {
      publicString.append(Integer.toHexString(0x0100 + (i & 0x00FF)).substring(1))
    }

    println(privateString)
    println(publicString)
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
