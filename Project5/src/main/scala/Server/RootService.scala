package Server

import java.security.{PublicKey, SecureRandom}

import Objects.ObjectJsonSupport._
import Objects._
import Server.Actors.{DebugInfo, DelegatorActor}
import Server.Messages.PutMsg
import Utils.{Base64Util, Constants, Crypto}
import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import spray.http.MediaTypes.`application/json`
import spray.io.ServerSSLEngineProvider
import spray.json._
import spray.routing._

import scala.collection.mutable
import scala.concurrent.duration._

trait RootService extends HttpService {
  val split = 8

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)

  private val random = new SecureRandom()

  private val serverKeyPair = Crypto.generateRSAKeys()

  implicit val myEngineProvider = ServerSSLEngineProvider { engine =>
    engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
    engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
    engine
  }

  val delegatorActor = Array.fill[ActorRef](split)(
    actorRefFactory.actorOf(
      Props(new DelegatorActor(null, serverKeyPair.getPublic))
    )
  )
  val da = DebugInfo()

  def dActor(pid: Int) = delegatorActor(pid % split)

  val userPublicKeys = mutable.HashMap[Int, PublicKey]()
  val defaultResponse = Crypto.constructSecureMessage(-1, "defaultResponse", serverKeyPair.getPublic, serverKeyPair.getPrivate)

  val myRoute = respondWithMediaType(`application/json`) {
    get {
      path("server_key") { rc => rc.complete(serverKeyPair.getPublic.getEncoded) } ~
        path("debug") { rc => rc.complete(da.toJson.compactPrint) } ~
        path("request") {
          entity(as[SecureMessage]) { secureMsg => rc =>
            val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, serverKeyPair.getPrivate)

            if (Crypto.verifySign(userPublicKeys(secureMsg.from), secureMsg.signature, requestKeyBytes)) {
              val requestKey = Crypto.constructAESKeyFromBytes(requestKeyBytes)
              val requestJson = Base64Util.decodeString(
                Crypto.decryptAES(secureMsg.message, requestKey, Constants.IV)
              )
              val secureRequest = JsonParser(requestJson).convertTo[SecureRequest]
            } else {
              rc.complete(defaultResponse)
            }
          }
        }
    } ~
      put {
        path("register") {
          entity(as[Array[Byte]]) { userPublicKeyBytes => rc =>
            val userPublicKey = Crypto.constructRSAPublicKeyFromBytes(userPublicKeyBytes)
            var userId = random.nextInt()
            while (userPublicKeys.contains(userId)) userId = random.nextInt()
            userPublicKeys.put(userId, userPublicKey)
            val jsonMsg = userId.toJson.compactPrint
            println(userPublicKeys.mkString(","))
            rc.complete(Crypto.constructSecureMessage(-1, jsonMsg, userPublicKey, serverKeyPair.getPrivate))
          }
        } ~
          entity(as[SecureMessage]) { secureMsg => rc =>
            val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, serverKeyPair.getPrivate)
            if (Crypto.verifySign(userPublicKeys(secureMsg.from), secureMsg.signature, requestKeyBytes)) {
              val requestKey = Crypto.constructAESKeyFromBytes(requestKeyBytes)
              dActor(secureMsg.from) ! PutMsg(rc, secureMsg.message, requestKey)
            } else {
              rc.complete(defaultResponse)
            }
          }
      }
  }
}