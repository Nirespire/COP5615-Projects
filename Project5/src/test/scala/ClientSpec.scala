import java.security.PublicKey

import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.{ObjectType, PostType}
import Objects._
import Server.RootService
import Utils.{Base64Util, Constants, Crypto, Resources}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers}
import spray.http.StatusCodes._
import spray.json._
import spray.testkit.ScalatestRouteTest

import scala.util.Random

class ClientSpec extends FreeSpec with ScalatestRouteTest with Matchers with RootService {
  def actorRefFactory = system

  var serverKey: PublicKey = null
  var user1Id = -1
  var user2Id = -1
  var myPageId = -1
  val user1KeyPair = Crypto.generateRSAKeys()
  val user2KeyPair = Crypto.generateRSAKeys()
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

  "Register User1" - {
    "when calling PUT /register" - {
      "should return user id" in {
        Put("/register", user1KeyPair.getPublic.getEncoded) ~> myRoute ~> check {
          status should equal(OK)
          val secureMsg = responseAs[SecureMessage]
          val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, user1KeyPair.getPrivate)
          Crypto.verifySign(serverKey, secureMsg.signature, requestKeyBytes) should equal(true)
          val requestKey = Crypto.constructAESKeyFromBytes(requestKeyBytes)
          val requestJson = Crypto.decryptAES(secureMsg.message, requestKey, Constants.IV)
          user1Id = Base64Util.decodeString(requestJson).toInt
          println(user1Id)
        }
      }
    }
  }

  "Put User1" - {
    "when calling PUT /user" - {
      "should create the new user assuming register has already happened" in {
        val fullName = Resources.names(Random.nextInt(Resources.names.length)).split(' ')
        val baseObj = new BaseObject(id = user1Id)
        val userObject = User("about", Resources.randomBirthday(), 'M', fullName(0), fullName(1), user1KeyPair.getPublic.getEncoded)
        val secureObject = Crypto.constructSecureObject(baseObj, ObjectType.user.id, userObject.toJson.compactPrint, Map(user1Id.toString -> user1KeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(user1Id, secureObject.toJson.compactPrint, serverKey, user1KeyPair.getPrivate)
        Put("/user", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)
        }
      }
    }
  }

  "Register User2" - {
    "when calling PUT /register" - {
      "should return user id" in {
        Put("/register", user2KeyPair.getPublic.getEncoded) ~> myRoute ~> check {
          status should equal(OK)
          val secureMsg = responseAs[SecureMessage]
          val requestKeyBytes = Crypto.decryptRSA(secureMsg.encryptedKey, user2KeyPair.getPrivate)
          Crypto.verifySign(serverKey, secureMsg.signature, requestKeyBytes) should equal(true)
          val requestKey = Crypto.constructAESKeyFromBytes(requestKeyBytes)
          val requestJson = Crypto.decryptAES(secureMsg.message, requestKey, Constants.IV)
          user2Id = Base64Util.decodeString(requestJson).toInt
          println(user2Id)
        }
      }
    }
  }

  "Put User2" - {
    "when calling PUT /user" - {
      "should create the new user assuming register has already happened" in {
        val fullName = Resources.names(Random.nextInt(Resources.names.length)).split(' ')
        val baseObj = new BaseObject(id = user2Id)
        val userObject = User("about", Resources.randomBirthday(), 'M', fullName(0), fullName(1), user2KeyPair.getPublic.getEncoded)
        val secureObject = Crypto.constructSecureObject(baseObj, ObjectType.user.id, userObject.toJson.compactPrint, Map(user2Id.toString -> user2KeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(user2Id, secureObject.toJson.compactPrint, serverKey, user2KeyPair.getPrivate)
        Put("create", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)
        }
      }
    }
  }

  "Register Page" - {
    "when calling PUT /register" - {
      "should return page id" in {
        Put("/register", pageKeyPair.getPublic.getEncoded) ~> myRoute ~> check {
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
        val baseObj = new BaseObject(id = myPageId)
        val pageObject = Page("about", Resources.getRandomPageCategory(), -1, pageKeyPair.getPublic.getEncoded)
        val secureObject = Crypto.constructSecureObject(baseObj, ObjectType.page.id, pageObject.toJson.compactPrint, Map(myPageId.toString -> pageKeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(myPageId, secureObject.toJson.compactPrint, serverKey, pageKeyPair.getPrivate)
        Put("/create", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)
        }
      }
    }
  }

  "Post UpdateFriendList by User1" - {
    "when calling Post /addFriend" - {
      "should add a profile to that user's friend list and allow them to view that user's content" in {
        Post("/addfriend") ~> myRoute ~> check {
          status should equal(OK)
        }
      }
    }
  }


  "Put Post by User1 viewable only by this user" - {
    "when calling PUT /post" - {
      "should return a post object viewable only by this user" in {
        val pid = user1Id
        val baseObject = new BaseObject()
        val postObject = Objects.Post(new DateTime().toString, Resources.getRandomStatus(), PostType.status, -1)
        val secureObject = Crypto.constructSecureObject(baseObject, ObjectType.post.id, postObject.toJson.compactPrint, Map(user1Id.toString -> user1KeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(user1Id, secureObject.toJson.compactPrint, serverKey, user1KeyPair.getPrivate)

        Put("/post", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)

          // TODO do 2 gets here, one this succeeds because they are authorized, the next that fails
        }
      }
    }
  }

  "Put Post by User1 viewable by all others" - {
    "when calling PUT /post" - {
      "should return a post object viewable this user, user2, and page" in {
        val pid = user1Id
        val baseObject = new BaseObject()
        val postObject = Objects.Post(new DateTime().toString, Resources.getRandomStatus(), PostType.status, -1)
        val secureObject = Crypto.constructSecureObject(baseObject, ObjectType.post.id, postObject.toJson.compactPrint,
          Map(user1Id.toString -> user1KeyPair.getPublic, user2Id.toString -> user2KeyPair.getPublic, myPageId.toString -> pageKeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(user1Id, secureObject.toJson.compactPrint, serverKey, user1KeyPair.getPrivate)

        Put("/post", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)

          // TODO do 2 gets here, both should succeed
        }
      }
    }
  }

  "Put Post by Page" - {
    "when calling PUT /post" - {
      "should return a post object viewable by everyone" in {
        val baseObject = new BaseObject()
        val pid = myPageId
        val postObject = Objects.Post(new DateTime().toString, Resources.getRandomStatus(), PostType.status, -1)
        val secureObject = Crypto.constructSecureObject(baseObject, ObjectType.post.id, postObject.toJson.compactPrint, Map(myPageId.toString -> pageKeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(pid, secureObject.toJson.compactPrint, serverKey, pageKeyPair.getPrivate)

        Put("/post", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)
        }
      }
    }
  }

  "Put Picture by User2 viewable only by this user" - {
    "when calling PUT /picture" - {
      "should return a picture object viewable only by this user" in {
        val baseObject = BaseObject()
        val pictureObject = Picture("filename.png", "")
        val secureObject = Crypto.constructSecureObject(baseObject, ObjectType.post.id, pictureObject.toJson.compactPrint, Map(user2Id.toString -> user2KeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(user2Id, secureObject.toJson.compactPrint, serverKey, user2KeyPair.getPrivate)
        Put("/post", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)
          // TODO do 2 gets here, one this succeeds because they are authorized, the next that fails
        }
      }
    }
  }

  "Put Album by User2 viewable only by this user" - {
    "when calling PUT /album" - {
      "should return a album object viewable by only this user" in {
        val baseObject = BaseObject()
        val albumObject = Album(new DateTime().toString, new DateTime().toString, -1, "desc")
        val secureObject = Crypto.constructSecureObject(baseObject, ObjectType.post.id, albumObject.toJson.compactPrint, Map(user2Id.toString -> user2KeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(user2Id, secureObject.toJson.compactPrint, serverKey, user2KeyPair.getPrivate)
        Put("/album", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)
          // TODO do 2 gets here, one this succeeds because they are authorized, the next that fails
        }

      }
    }
  }

  "Post User1" - {
    "when calling POST /user" - {
      "should update User object" in {
        val baseObject = new BaseObject(id = user1Id)
        val fullName = Resources.names(Random.nextInt(Resources.names.length)).split(' ')
        val userObject = User("about", Resources.randomBirthday(), 'M', fullName(0), fullName(1), user1KeyPair.getPublic.getEncoded)
        val secureObject = Crypto.constructSecureObject(baseObject, ObjectType.user.id, userObject.toJson.compactPrint, Map(user1Id.toString -> user1KeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(user1Id, secureObject.toJson.compactPrint, serverKey, user1KeyPair.getPrivate)
        Post("/user", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)
        }
      }
    }
  }

  "Post Page by page" - {
    "when calling POST /page" - {
      "should update Page object" in {
        val baseObject = BaseObject(id = myPageId)
        val pageObject = Page("about", Resources.getRandomPageCategory(), -1, pageKeyPair.getPublic.getEncoded)
        val secureObject = Crypto.constructSecureObject(baseObject, ObjectType.page.id, pageObject.toJson.compactPrint, Map(myPageId.toString -> pageKeyPair.getPublic))
        val secureMessage = Crypto.constructSecureMessage(myPageId, secureObject.toJson.compactPrint, serverKey, pageKeyPair.getPrivate)
        Post("/page", secureMessage) ~> myRoute ~> check {
          status should equal(OK)
          println(entity)
        }
      }
    }
  }

  //  "Post Picture by User2" - {
  //    "when calling POST /picture" - {
  //      "should update Picture object" in {
  //        val pictureObject = Picture(BaseObject(), user2Id, -1, "filename.png", "")
  //        val secureObject = Crypto.constructSecureObject(pictureObject.baseObject, ObjectType.post.id, pictureObject.toJson.compactPrint, Map(user2Id.toString -> user2KeyPair.getPublic))
  //        val secureMessage = Crypto.constructSecureMessage(user2Id, secureObject.toJson.compactPrint, serverKey, user2KeyPair.getPrivate)
  //        Post("/picture", secureMessage) ~> myRoute ~> check{
  //          status should equal(OK)
  //          println(entity)
  //          // TODO do 2 gets here, one this succeeds because they are authorized, the next that fails
  //        }
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
