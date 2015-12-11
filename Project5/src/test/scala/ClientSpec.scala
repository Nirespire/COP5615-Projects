import java.nio.ByteBuffer
import java.security.PublicKey
import Objects._
import Server.RootService
import Utils.{Resources, Constants, Base64Util, Crypto}
import org.scalatest.{Matchers, FreeSpec}
import spray.http.StatusCodes._
import spray.testkit.ScalatestRouteTest
import ObjectJsonSupport._
import spray.json._
import Objects.ObjectTypes.ObjectType

import scala.util.Random

class ClientSpec extends FreeSpec with ScalatestRouteTest with Matchers with RootService {
  def actorRefFactory = system

  var serverKey: PublicKey = null
  var myUserId = -1
  var myPageId = -1
  val userKeyPair = Crypto.generateRSAKeys()
  val pageKeyPair = Crypto.generateRSAKeys()

  "Get Server Key" - {
    "when calling GET /server_key" - {
      "should return the server's public key" in {
        Get("/server_key") ~> myRoute ~> check {
          status should equal(OK)
          val returnObject = responseAs[Array[Byte]]
          println(Base64Util.encodeString(returnObject))
          serverKey = Crypto.constructRSAPublicKeyFromBytes(returnObject)
        }
      }
    }
  }

  "Register User" - {
    "when calling PUT /register" - {
      "should return user id" in {
        Put("/register", userKeyPair.getPublic.getEncoded) ~> myRoute ~> check{
          status should equal(OK)
          val secureMsg = responseAs[SecureMessage]
          val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, userKeyPair.getPrivate)
          Crypto.verifySign(serverKey, secureMsg.signature, requestKeyBytes) should equal(true)
          val requestKey = Crypto.constructAESKeyFromBytes(requestKeyBytes)
          val requestJson = Crypto.decryptAES(secureMsg.message, requestKey, Constants.IV)
          myUserId = Base64Util.decodeString(requestJson).toInt
          println(myUserId)
        }
      }
    }
  }

  "Put User" - {
    "when calling PUT /user" - {
      "should create the new user assuming register has already happened" in {
        val fullName = Resources.names(Random.nextInt(Resources.names.length)).split(' ')
        val userObject = User(new BaseObject(id = myUserId), "about", Resources.randomBirthday(), 'M', fullName(0), fullName(1), userKeyPair.getPublic.getEncoded)
        val secureObject = Crypto.constructSecureObject(userObject.baseObject, ObjectType.user.id, userObject.toJson.compactPrint, Map(myUserId.toString -> userKeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(myUserId, secureObject.toJson.compactPrint, serverKey, userKeyPair.getPrivate)
        Put("/user", secureMessage) ~> myRoute ~> check{
          status should equal(OK)
          println(entity)
        }
      }
    }
  }

  "Register Page" - {
    "when calling PUT /register" - {
      "should return page id" in {
        Put("/register", pageKeyPair.getPublic.getEncoded) ~> myRoute ~> check{
          status should equal(OK)
          val secureMsg = responseAs[SecureMessage]
          val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, pageKeyPair.getPrivate)
          Crypto.verifySign(serverKey, secureMsg.signature, requestKeyBytes) should equal(true)
          val requestKey = Crypto.constructAESKeyFromBytes(requestKeyBytes)
          val requestJson = Crypto.decryptAES(secureMsg.message, requestKey, Constants.IV)
          myPageId = Base64Util.decodeString(requestJson).toInt
          println(myPageId)
        }
      }
    }
  }

  "Put Page" - {
    "when calling PUT /page" - {
      "should create the new page assuming register has already happened" in {
        val pageObject = Page(new BaseObject(id = myPageId), "about", Resources.getRandomPageCategory(), -1, pageKeyPair.getPublic.getEncoded)
        val secureObject = Crypto.constructSecureObject(pageObject.baseObject, ObjectType.page.id, pageObject.toJson.compactPrint, Map(myPageId.toString -> pageKeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(myPageId, secureObject.toJson.compactPrint, serverKey, pageKeyPair.getPrivate)
        Put("/page", secureMessage) ~> myRoute ~> check{
          status should equal(OK)
          println(entity)
        }
      }
    }
  }



//  "Put Post" - {
//    "when calling PUT /post" - {
//      "should return a post object" in {
//
//      }
//    }
//  }
//
//  "Put Picture" - {
//    "when calling PUT /picture" - {
//      "should return a picture object" in {
//
//      }
//    }
//  }
//
//  "Put Album" - {
//    "when calling PUT /album" - {
//      "should return a album object" in {
//
//      }
//    }
//  }
//
//  "Post User" - {
//    "when calling POST /user" - {
//      "should return a user object" in {
//
//      }
//    }
//  }
//
//  "Post Page" - {
//    "when calling POST /page" - {
//      "should return a page object" in {
//
//      }
//    }
//  }
//
//  "Post Picture" - {
//    "when calling POST /picture" - {
//      "should return a picture object" in {
//
//      }
//    }
//  }
//
//  "Post Album" - {
//    "when calling Post /album" - {
//      "should return a album object" in {
//
//      }
//    }
//  }
//
//  "Create friendship between 0 and 1" - {
//    "when calling Post /friendlist" - {
//      "should return an UpdateFriendlist object" in {
//
//      }
//    }
//  }
//
//  "Get profile 0" - {
//    "when getting profile 0" - {
//      "should return user 0 object" in {
//
//      }
//    }
//  }
//
//
//  "Get feed for 0" - {
//    "when getting feed for user 0" - {
//      "should return latest post object" in {
//
//
//      }
//    }
//  }
//
//  "Get post 0 for 1" - {
//    "when getting post 0 for user 0" - {
//      "should return post 0 object" in {
//
//      }
//    }
//  }
//  "Get album 0 for 0" - {
//    "when getting album 0 for user 0" - {
//      "should return album 0 object" in {
//
//      }
//    }
//  }
//  "Get picture 1 for profile 0" - {
//    "when getting picture 1 for user 0" - {
//      "should return picture 1 object" in {
//
//      }
//    }
//  }
//
//  "1 likes 0's post" - {
//    "when calling PUT /like for a post" - {
//      "should return a post object" in {
//
//      }
//    }
//  }
//
//  "Delete Post" - {
//    "when calling DELETE /post" - {
//      "should return a post object" in {
//
//      }
//    }
//  }
//
//  "Delete Picture" - {
//    "when calling DELETE /picture" - {
//      "should return a picture object" in {
//
//      }
//    }
//  }
//
//  "Delete Album" - {
//    "when calling DELETE /album" - {
//      "should return a album object" in {
//
//      }
//    }
//  }
//
//  "Delete User" - {
//    "when calling DELETE /user" - {
//      "should return a user object each" in {
//
//      }
//    }
//  }
//
//  "Delete Page" - {
//    "when calling DELETE /page" - {
//      "should return a page object" in {
//
//      }
//    }
//  }
}
