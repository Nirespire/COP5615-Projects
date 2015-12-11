import java.nio.ByteBuffer

import Objects._
import Server.RootService
import Utils.{Constants, Base64Util, Crypto}
import org.joda.time.DateTime
import org.scalatest.{Matchers, FreeSpec}
import spray.http.StatusCodes._
import spray.json.JsonParser
import spray.testkit.ScalatestRouteTest
import ObjectJsonSupport._

class ClientSpec extends FreeSpec with ScalatestRouteTest with Matchers with RootService {
  def actorRefFactory = system

  "Get Server Key" - {
    "when calling GET /server_key" - {
      "should return the server's public key" in {
        Get("/server_key") ~> myRoute ~> check {
          status should equal(OK)
          val returnObject = responseAs[Array[Byte]]
          println(Base64Util.encodeString(returnObject))
          val serverKey = Crypto.constructRSAKeyFromBytes(returnObject)
        }
      }
    }
  }



  "Register User" - {
    "when calling PUT /register" - {
      "should return a random string to sign" in {
        Get("/server_key") ~> myRoute ~> check {
          status should equal(OK)
          val returnObject = responseAs[Array[Byte]]
          val serverKey = Crypto.constructRSAPublicKeyFromBytes(returnObject)

          val keyPair = Crypto.generateRSAKeys()

          Put("/register", keyPair.getPublic.getEncoded) ~> myRoute ~> check{
            status should equal(OK)

            val secureMsg = responseAs[SecureMessage]
            val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, keyPair.getPrivate)
            Crypto.verifySign(serverKey, secureMsg.signature, requestKeyBytes) should equal(true)

            val requestKey = Crypto.constructAESKeyFromBytes(requestKeyBytes)

            val requestJson = Crypto.decryptAES(secureMsg.message, requestKey, Constants.IV)

            val userID = ByteBuffer.wrap(requestJson).getInt

            println(userID)

          }
        }
      }
    }
  }

  "Put Post" - {
    "when calling PUT /post" - {
      "should return a post object" in {

      }
    }
  }

  "Put Picture" - {
    "when calling PUT /picture" - {
      "should return a picture object" in {

      }
    }
  }

  "Put Album" - {
    "when calling PUT /album" - {
      "should return a album object" in {

      }
    }
  }

  "Post User" - {
    "when calling POST /user" - {
      "should return a user object" in {

      }
    }
  }

  "Post Page" - {
    "when calling POST /page" - {
      "should return a page object" in {

      }
    }
  }

  "Post Picture" - {
    "when calling POST /picture" - {
      "should return a picture object" in {

      }
    }
  }

  "Post Album" - {
    "when calling Post /album" - {
      "should return a album object" in {

      }
    }
  }

  "Create friendship between 0 and 1" - {
    "when calling Post /friendlist" - {
      "should return an UpdateFriendlist object" in {

      }
    }
  }

  "Get profile 0" - {
    "when getting profile 0" - {
      "should return user 0 object" in {

      }
    }
  }


  "Get feed for 0" - {
    "when getting feed for user 0" - {
      "should return latest post object" in {


      }
    }
  }

  "Get post 0 for 1" - {
    "when getting post 0 for user 0" - {
      "should return post 0 object" in {

      }
    }
  }
  "Get album 0 for 0" - {
    "when getting album 0 for user 0" - {
      "should return album 0 object" in {

      }
    }
  }
  "Get picture 1 for profile 0" - {
    "when getting picture 1 for user 0" - {
      "should return picture 1 object" in {

      }
    }
  }

  "1 likes 0's post" - {
    "when calling PUT /like for a post" - {
      "should return a post object" in {

      }
    }
  }

  "Delete Post" - {
    "when calling DELETE /post" - {
      "should return a post object" in {

      }
    }
  }

  "Delete Picture" - {
    "when calling DELETE /picture" - {
      "should return a picture object" in {

      }
    }
  }

  "Delete Album" - {
    "when calling DELETE /album" - {
      "should return a album object" in {

      }
    }
  }

  "Delete User" - {
    "when calling DELETE /user" - {
      "should return a user object each" in {

      }
    }
  }

  "Delete Page" - {
    "when calling DELETE /page" - {
      "should return a page object" in {

      }
    }
  }
}
