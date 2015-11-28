import Server.RootService
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, Matchers}
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes._
import Objects._
import Objects.ObjectJsonSupport._

class ServerSpec extends FreeSpec with ScalatestRouteTest with Matchers with RootService {
  def actorRefFactory = system

  "Put 3 Users" - {
    "when calling PUT /user" - {
      "should return a user object each" in {
        Put("/user", User(BaseObject(), "Im user 1", "birthday", 'M', "first name1", "last name1")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }

        Put("/user", User(BaseObject(), "Im user 2", "birthday", 'M', "first name2", "last name2")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }

        Put("/user", User(BaseObject(), "Im user 3", "birthday", 'M', "first name3", "last name3")) ~> myRoute ~> check {
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
        Put("/post", Objects.Post(BaseObject(), 0, new DateTime().toString(), 0, "status", Objects.ObjectTypes.PostType.status, -1)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Put Picture" - {
    "when calling PUT /picture" - {
      "should return a picture object" in {
        Put("/picture", Picture(BaseObject(), 0, -1, "filename", "blah")) ~> myRoute ~> check {
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
        Post("/picture", Picture(BaseObject(0), 0, -1, "filename", "blah")) ~> myRoute ~> check {
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

  "Create friendship between 0 and 1" - {
    "when calling Post /friendlist" - {
      "should return an UpdateFriendlist object" in {
        Post("/addfriend", UpdateFriendList(0,1)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Get profile 0" - {
    "when getting profile 0" - {
      "should return user 0 object" in {
        Get("/user/0") ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }


  "Get feed for 0" - {
    "when getting feed for user 0" - {
      "should return latest post object" in {
        Get("/feed/0") ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Get post 0 for 0" - {
    "when getting post 0 for user 0" - {
      "should return post 0 object" in {
        Get("/post/0/0") ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }
  "Get album 0 for 0" - {
    "when getting album 0 for user 0" - {
      "should return album 0 object" in {
        Get("/album/0/0") ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }
  "Get picture 0 for 0" - {
    "when getting picture 0 for user 0" - {
      "should return picture 0 object" in {
        Get("/picture/0/0") ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }


  "Delete User" - {
    "when calling DELETE /user" - {
      "should return a user object each" in {
        Delete("/user", User(BaseObject(), "Im user 1", "birthday", 'M', "first name1", "last name1")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Delete Page" - {
    "when calling DELETE /page" - {
      "should return a page object" in {
        Delete("/page", Page(BaseObject(), "about", "category", -1)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Delete Post" - {
    "when calling DELETE /post" - {
      "should return a post object" in {
        Delete("/post", Objects.Post(BaseObject(), 0, new DateTime().toString(), 0, "status", Objects.ObjectTypes.PostType.status, -1)) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Delete Picture" - {
    "when calling DELETE /picture" - {
      "should return a picture object" in {
        Delete("/picture", Picture(BaseObject(), 0, -1, "filename", "blah")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }

  "Delete Album" - {
    "when calling DELETE /album" - {
      "should return a album object" in {
        Delete("/album", Album(BaseObject(), 0, new DateTime().toString(), new DateTime().toString(), 0, "description")) ~> myRoute ~> check {
          status should equal(OK)
          println(entity.toString())
        }
      }
    }
  }
}

