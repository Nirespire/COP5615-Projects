package Server

import java.security.{PublicKey, SecureRandom}
import javax.crypto.SecretKey
import Objects.ObjectJsonSupport._
import Objects._
import Server.Actors.DelegatorActor
import Server.Messages._
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


  implicit val myEngineProvider = ServerSSLEngineProvider { engine =>
    engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
    engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
    engine
  }

  val da = DebugInfo()

  val delegatorActor = Array.fill[ActorRef](split)(
    actorRefFactory.actorOf(
      Props(new DelegatorActor(da, Constants.serverPublicKey))
    )
  )

  def dActor(pid: Int) = delegatorActor(pid % split)

  val defaultResponse = Crypto.constructSecureMessage(
    -1,
    "defaultResponse",
    Constants.serverPublicKey,
    Constants.serverPrivateKey
  )

  val myRoute = respondWithMediaType(`application/json`) {
    get {
      path("server_key") {
        rc => rc.complete(Constants.serverPublicKey.getEncoded)
      } ~ path("debug") {
        respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) { rc =>
          rc.complete(da.toJson.compactPrint)
        }
      } ~ path("request") {
        entity(as[SecureMessage]) { secureMsg => rc =>
          val jsonMsg = verifyMessage(secureMsg)
          if (jsonMsg.nonEmpty) {
            val secureReq = JsonParser(jsonMsg).convertTo[SecureRequest]
            dActor(secureReq.to) ! GetSecureObjMsg(rc, secureReq)
          } else {
            rc.complete(defaultResponse)
          }
        }
      } ~ path("getpublickey" / IntNumber) { pid => rc =>
        rc.complete(
          Crypto.constructSecureMessage(
            Constants.serverId,
            Constants.userPublicKeys(pid).getEncoded.toJson.compactPrint,
            Constants.userPublicKeys(pid),
            Constants.serverPrivateKey
          )
        )
      } ~ path("friends") {
        entity(as[SecureMessage]) { secureMsg => rc =>
          val jsonMsg = verifyMessage(secureMsg)
          if (jsonMsg.nonEmpty) {
            // TODO get friends public key
            // TODO POST for user and page
            // TODO handle addFriend
            // TODO handle likes
            // TODO create artificial illegal requests, generate random from and record in debug
            // TODO client GET and DELETE = SecureMessage(SecureRequest))
            // TODO client request within simulator to get access to old object
          } else {
            rc.complete(defaultResponse)
          }
        }
      }
    } ~ put {
      path("register") {
        entity(as[Array[Byte]]) { userPublicKeyBytes => rc =>
          da.debugVar(Constants.registerChar) += 1
          val userPublicKey = Crypto.constructRSAPublicKeyFromBytes(userPublicKeyBytes)
          var userId = Math.abs(random.nextInt())
          while (Constants.userPublicKeys.contains(userId)) userId = Math.abs(random.nextInt())
          Constants.userPublicKeys.put(userId, userPublicKey)
          val jsonMsg = userId.toJson.compactPrint
          rc.complete(Crypto.constructSecureMessage(-1, jsonMsg, userPublicKey, Constants.serverPrivateKey))
        }
      } ~ entity(as[SecureMessage]) { secureMsg => rc =>
        val jsonMsg = verifyMessage(secureMsg)
        if (jsonMsg.nonEmpty) {
          val secureObj = JsonParser(jsonMsg).convertTo[SecureObject]
          dActor(secureObj.to) ! PutSecureObjMsg(rc, secureObj)
        } else {
          rc.complete(defaultResponse)
        }
      }
    } ~ post {
      entity(as[SecureMessage]) { secureMsg => rc =>
        val jsonMsg = verifyMessage(secureMsg)
        if (jsonMsg.nonEmpty) {
          val secureObj = JsonParser(jsonMsg).convertTo[SecureObject]
          dActor(secureObj.to) ! PostSecureObjMsg(rc, secureObj)
        } else {
          rc.complete(defaultResponse)
        }
      }
    } ~ delete {
      entity(as[SecureMessage]) { secureMsg => rc =>
        val jsonMsg = verifyMessage(secureMsg)
        if (jsonMsg.nonEmpty) {
          val secureReq = JsonParser(jsonMsg).convertTo[SecureRequest]
          dActor(secureReq.to) ! DeleteSecureObjMsg(rc, secureReq)
        } else {
          rc.complete(defaultResponse)
        }
      }
    }
  }

  def verifyMessage(secureMsg: SecureMessage): String = {
    val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, Constants.serverPrivateKey)
    if (Crypto.verifySign(Constants.userPublicKeys(secureMsg.from), secureMsg.signature, requestKeyBytes)) {
      Base64Util.decodeString(
        Crypto.decryptAES(secureMsg.message, Crypto.constructAESKeyFromBytes(requestKeyBytes), Constants.IV)
      )
    } else {
      ""
    }
  }
}