import Server.Messages.ResponseMessage
import Server.RootService
import Utils.{Base64Util, Crypto}
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers}
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes._
import Objects._
import Objects.ObjectJsonSupport._
import java.security.Signature

class ServerSpec extends FreeSpec with ScalatestRouteTest with Matchers with RootService {
  def actorRefFactory = system

  "Put 3 Users" - {
    "when calling PUT /user" - {
      "should return a user object each" in {
        Put("/user", User(BaseObject(), "Im user 1", "birthday", 'M', "first name1", "last name1", "testkey".getBytes)) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[User].baseObject.id should equal(0)
          println(entity.toString)
        }

        Put("/user", User(BaseObject(), "Im user 2", "birthday", 'M', "first name2", "last name2", "testkey".getBytes)) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[User].baseObject.id should equal(1)
          println(entity.toString)
        }

        Put("/user", User(BaseObject(), "Im user 3", "birthday", 'M', "first name3", "last name3", "testkey".getBytes())) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[User].baseObject.id should equal(2)
          println(entity.toString)
        }
      }
    }
  }

  "Put Page" - {
    "when calling PUT /page" - {
      "should return a page object" in {
        Put("/page", Page(BaseObject(), "about", "category", -1, "testkey".getBytes())) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Page].baseObject.id should equal(3)
          println(entity.toString)
        }
      }
    }
  }

  "Register User" - {
    "when calling PUT /register" - {
      "should return a random string to sign" in {
        var randomString: String = null
        val clientPair = Crypto.generateRSAKeys()
        val newUser = User(BaseObject(), "Im user 4", "birthday", 'M', "first name14", "last name4", clientPair.getPublic().getEncoded())

        Put("/register", newUser) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[User].baseObject.id should equal(0)

          response.headers.foreach { i =>
            if (i.lowercaseName == "randomstring") {
              randomString = i.value
            }
          }
          println(randomString)

          val signedData = Crypto.signData(clientPair.getPrivate(), Base64Util.encodeBinary(randomString))

          val signedString = Base64Util.encodeString(signedData)

          println(signedString)

          // TO SERVER

          val clientSignedData = Base64Util.decodeBinary(signedString)

          val clientPublicKey = Crypto.constructRSAPublicKeyFromBytes(clientPair.getPublic().getEncoded())

          val isVerified = Crypto.verifySign(clientPublicKey, clientSignedData, Base64Util.encodeBinary(randomString))

          isVerified should equal(true)

        }
      }
    }
  }

  "Put Post" - {
    "when calling PUT /post" - {
      "should return a post object" in {
        Put("/post", Objects.Post(BaseObject(), 0, new DateTime().toString, 0, "status", Objects.ObjectTypes.PostType.status, -1)) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Post].baseObject.id should equal(2)
          println(entity.toString)
        }
      }
    }
  }

  "Put Picture" - {
    "when calling PUT /picture" - {
      "should return a picture object" in {
        Put("/picture", Picture(BaseObject(), 0, -1, "filename", "blah")) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Picture].baseObject.id should equal(2)
          println(entity.toString)
        }
      }
    }
  }

  "Put Album" - {
    "when calling PUT /album" - {
      "should return a album object" in {
        Put("/album", Album(BaseObject(), 0, new DateTime().toString, new DateTime().toString, 0, "description")) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Album].baseObject.id should equal(3)
          println(entity.toString)
        }
      }
    }
  }

  "Post User" - {
    "when calling POST /user" - {
      "should return a user object" in {
        Post("/user", User(BaseObject(0), "something updated", "birthday", 'M', "first name", "last name", "testkey".getBytes())) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[User].about should equal("something updated")
          println(entity.toString)
        }
      }
    }
  }

  "Post Page" - {
    "when calling POST /page" - {
      "should return a page object" in {
        Post("/page", Page(BaseObject(3), "something updated", "category", 0, "testkey".getBytes())) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Page].about should equal("something updated")
          responseAs[Page].cover should equal(0)
          println(entity.toString)
        }
      }
    }
  }

  "Post Picture" - {
    "when calling POST /picture" - {
      "should return a picture object" in {
        Post("/picture", Picture(BaseObject(0), 0, -1, "updated filename", "blah")) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Picture].filename should equal("updated filename")
          println(entity.toString)
        }
      }
    }
  }

  "Post Album" - {
    "when calling Post /album" - {
      "should return a album object" in {
        Post("/album", Album(BaseObject(0), 0, new DateTime().toString, new DateTime().toString, 0, "updated description")) ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Album].description should equal("updated description")
          println(entity.toString)
        }
      }
    }
  }

  "Create friendship between 0 and 1" - {
    "when calling Post /friendlist" - {
      "should return an UpdateFriendlist object" in {
        Post("/addfriend", UpdateFriendList(0, 1)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString)
        }

        Post("/addfriend", UpdateFriendList(1, 0)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString)
        }

      }
    }
  }

  "Get profile 0" - {
    "when getting profile 0" - {
      "should return user 0 object" in {
        Get("/user/0") ~> myRoute ~> check {
          status should equal(OK)
          responseAs[User].baseObject.id should equal(0)
          println(entity.toString)
        }
      }
    }
  }


  "Get feed for 0" - {
    "when getting feed for user 0" - {
      "should return latest post object" in {
        Get("/feed/0") ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Seq[Int]] should contain(1)
          println(entity.toString)

          Get("/post/0/1") ~> myRoute ~> check {
            status should equal(OK)
            responseAs[Post].baseObject.id should equal(1)
            println(entity.toString)
          }
        }
      }
    }
  }

  "Get post 0 for 1" - {
    "when getting post 0 for user 0" - {
      "should return post 0 object" in {
        Get("/post/0/1") ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Post].baseObject.id should equal(1)
          println(entity.toString)
        }
      }
    }
  }
  "Get album 0 for 0" - {
    "when getting album 0 for user 0" - {
      "should return album 0 object" in {
        Get("/album/0/0") ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Album].baseObject.id should equal(0)
          println(entity.toString)
        }
      }
    }
  }
  "Get picture 1 for profile 0" - {
    "when getting picture 1 for user 0" - {
      "should return picture 1 object" in {
        Get("/picture/0/1") ~> myRoute ~> check {
          status should equal(OK)
          responseAs[Picture].baseObject.id should equal(1)
          println(entity.toString)
        }
      }
    }
  }

  "1 likes 0's post" - {
    "when calling PUT /like for a post" - {
      "should return a post object" in {
        Put("/like/0/post/1/1") ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString)

          Get("/post/0/1") ~> myRoute ~> check {
            status should equal(OK)
            responseAs[Post].baseObject.likes.contains(1)
            println(entity.toString)
          }
        }
      }
    }
  }

  "Delete Post" - {
    "when calling DELETE /post" - {
      "should return a post object" in {
        Delete("/post", Objects.Post(BaseObject(), 0, new DateTime().toString, 0, "status", Objects.ObjectTypes.PostType.status, -1)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString)
        }
      }
    }
  }

  "Delete Picture" - {
    "when calling DELETE /picture" - {
      "should return a picture object" in {
        Delete("/picture", Picture(BaseObject(), 0, -1, "filename", "blah")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString)
        }
      }
    }
  }

  "Delete Album" - {
    "when calling DELETE /album" - {
      "should return a album object" in {
        Delete("/album", Album(BaseObject(), 0, new DateTime().toString, new DateTime().toString, 0, "description")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString)
        }
      }
    }
  }

  "Delete User" - {
    "when calling DELETE /user" - {
      "should return a user object each" in {
        Delete("/user", User(BaseObject(id = 0), "Im user 1", "birthday", 'M', "first name1", "last name1", "testkey".getBytes)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString)
        }
      }
    }
  }

  "Delete Page" - {
    "when calling DELETE /page" - {
      "should return a page object" in {
        Delete("/page", Page(BaseObject(id = 3), "about", "category", -1, "testkey".getBytes())) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString)
        }
      }
    }
  }
}