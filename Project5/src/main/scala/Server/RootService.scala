package Server

import java.security.{PublicKey, SecureRandom}
import javax.crypto.SecretKey
import Objects.ObjectJsonSupport._
import Objects._
import Server.Actors.DelegatorActor
import Server.Messages.{DeleteEncryptedMsg, PostEncryptedMsg, PutEncryptedMsg}
import Utils.{DebugInfo, Base64Util, Constants, Crypto}
import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import spray.http.HttpHeaders.RawHeader
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

  val da = DebugInfo()

  val delegatorActor = Array.fill[ActorRef](split)(
    actorRefFactory.actorOf(
      Props(new DelegatorActor(da, serverKeyPair.getPublic))
    )
  )

  def dActor(pid: Int) = delegatorActor(pid % split)

  val userPublicKeys = mutable.HashMap[Int, PublicKey]()
  val defaultResponse = Crypto.constructSecureMessage(-1, "defaultResponse", serverKeyPair.getPublic, serverKeyPair.getPrivate)

  val myRoute = respondWithMediaType(`application/json`) {
    get {
      path("server_key") { rc => rc.complete(serverKeyPair.getPublic.getEncoded) } ~
        path("debug") {
          respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) { rc =>
            rc.complete(da.toJson.compactPrint)
          }
        } ~ path("request") {
        entity(as[SecureMessage]) { secureMsg => rc =>
          val (verified, aesKey) = verifyMessage(rc, secureMsg)
          if (verified) {
            val requestJson = Base64Util.decodeString(
              Crypto.decryptAES(secureMsg.message, aesKey, Constants.IV)
            )
            val secureRequest = JsonParser(requestJson).convertTo[SecureRequest]
          }
        }
      } ~ path("friends") {
        entity(as[SecureMessage]) { secureMsg => rc =>
          val (verified, aesKey) = verifyMessage(rc, secureMsg)
          if (verified) {
            // TODO get public key
            // TODO POST for user and page
            // TODO handle addFriend
            // TODO handle likes
            // TODO create artificial illegal requests, generate random from and record in debug
            // TODO client GET and DELETE = SecureMessage(SecureRequest))
            // TODO client request within simulator to get access to old object
          }
        }
      }
    } ~ put {
      path("register") {
        entity(as[Array[Byte]]) { userPublicKeyBytes => rc =>
          da.debugVar(Constants.registerChar) += 1
          val userPublicKey = Crypto.constructRSAPublicKeyFromBytes(userPublicKeyBytes)
          var userId = Math.abs(random.nextInt())
          while (userPublicKeys.contains(userId)) userId = Math.abs(random.nextInt())
          userPublicKeys.put(userId, userPublicKey)
          val jsonMsg = userId.toJson.compactPrint
          rc.complete(Crypto.constructSecureMessage(-1, jsonMsg, userPublicKey, serverKeyPair.getPrivate))
        }
      } ~ entity(as[SecureMessage]) { secureMsg => rc =>
        val (verified, aesKey) = verifyMessage(rc, secureMsg)
        if (verified) {
          dActor(secureMsg.from) ! PutEncryptedMsg(rc, secureMsg.from, secureMsg.message, aesKey)
        }
      }
    } ~ post {
      entity(as[SecureMessage]) { secureMsg => rc =>
        val (verified, aesKey) = verifyMessage(rc, secureMsg)
        if (verified) {
          dActor(secureMsg.from) ! PostEncryptedMsg(rc, secureMsg.from, secureMsg.message, aesKey)
        }
      }
    } ~ delete {
      entity(as[SecureMessage]) { secureMsg => rc =>
        val (verified, aesKey) = verifyMessage(rc, secureMsg)
        if (verified) {
          dActor(secureMsg.from) ! DeleteEncryptedMsg(rc, secureMsg.from, secureMsg.message, aesKey)
        }
      }
    }
  }

  def verifyMessage(rc: RequestContext, secureMsg: SecureMessage): (Boolean, SecretKey) = {
    val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, serverKeyPair.getPrivate)
    if (Crypto.verifySign(userPublicKeys(secureMsg.from), secureMsg.signature, requestKeyBytes)) {
      (Constants.trueBool, Crypto.constructAESKeyFromBytes(requestKeyBytes))
    } else {
      rc.complete(defaultResponse)
      (Constants.falseBool, Constants.defaultKey)
    }
  }
}