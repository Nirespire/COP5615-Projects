import Server.RootService
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers}
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes._
import Objects._
import Objects.ObjectJsonSupport._

class ServerSpec extends FreeSpec with ScalatestRouteTest with Matchers with RootService {
  def actorRefFactory = system

  "Put User" - {
    "when calling PUT /user" - {
      "should return a user object" in {
        Put("/user", User(BaseObject(), "about me", "birthday", 'M', "first name", "last name")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Put Page" - {
    "when calling PUT /page" - {
      "should return a page object" in {
        Put("/page", Page(BaseObject(), "about", "category", -1)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Put Post" - {
    "when calling PUT /post" - {
      "should return a post object" in {
        Put("/user", User(BaseObject(), "about me", "birthday", 'M', "first name", "last name")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
          Put("/post", Objects.Post(BaseObject(), 2, new DateTime().toString(), 2, "status", Objects.ObjectTypes.PostType.status, -1)) ~> myRoute ~> check {
            status should equal(OK)
            println(entity.toString())
          }
        }
      }
    }
  }

  "Put Picture" - {
    "when calling PUT /picture" - {
      "should return a picture object" in {
        Put("/picture", Picture(BaseObject(),0, -1, "filename", "blah")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Put Album" - {
    "when calling PUT /album" - {
      "should return a album object" in {
        Put("/album", Album(BaseObject(), 0, new DateTime().toString(), new DateTime().toString(), 0, "description")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }


  "Post User" - {
    "when calling POST /user" - {
      "should return a user object" in {
        Post("/user", User(BaseObject(0), "about me", "birthday", 'M', "first name", "last name")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Post Page" - {
    "when calling POST /page" - {
      "should return a page object" in {
        Post("/page", Page(BaseObject(0), "about", "category", -1)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Post Picture" - {
    "when calling POST /picture" - {
      "should return a picture object" in {
        Post("/picture", Picture(BaseObject(0),0, -1, "filename", "blah")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Post Album" - {
    "when calling Post /album" - {
      "should return a album object" in {
        Post("/album", Album(BaseObject(0), 0, new DateTime().toString(), new DateTime().toString(), 0, "description")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }


}
