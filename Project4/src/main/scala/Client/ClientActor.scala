package Client

import java.security.KeyPairGenerator
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

import Client.ClientType.ClientType
import Client.Messages._
import Client.Resources._
import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.PostType._
import Objects._
import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import spray.client.pipelining
import spray.client.pipelining._
import spray.json._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}


class ClientActor(isPage: Boolean = false, clientType: ClientType) extends Actor with ActorLogging {

  val trueBool = true
  val falseBool = false
  val myPages = mutable.ArrayBuffer[Page]()
  val myFriends = mutable.ArrayBuffer[Int]()
  val myRealFriends = mutable.HashMap[ActorRef, Int]()
  var me: User = null
  var mePage: Page = null
  var myBaseObj: BaseObject = null
  var numPosts = 0
  var numAlbums = 0

  val (putPercent, getPercent, friendPercent, updatePercent) = clientType match {
    case ClientType.Active => (80, 50, 90, 50)
    case ClientType.Passive => (20, 90, 80, 5)
    case ClientType.ContentCreator => (70, 20, 10, 40)
  }

  val config = ConfigFactory.load()
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  import context.dispatcher

  def random(n: Int) = Random.nextInt(n)

  def durationSeconds(n: Int) = n.seconds

  def randomDuration(n: Int) = durationSeconds(random(n))

  def receive = {
    // Create a user profile or page for self
    case true => registerMyself()

    // Do some activity every second
    case false =>
      if (myBaseObj == null) {
        context.system.scheduler.scheduleOnce(durationSeconds(1), self, false)
      } else {
        //        log.info(myBaseObj.id + " starting activity")
        val probability = Random.nextInt(101)
        if (isPage) get(s"page/${myBaseObj.id}", "page") else get(s"user/${myBaseObj.id}", "user")

        if (probability < putPercent) {
          val rand = random(4)
          rand match {
            case 0 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePost(status, -1))
            case 1 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakeAlbum)
            case 2 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePicture(-1))
            case 3 => context.system.scheduler.scheduleOnce(randomDuration(3), self, MakePicturePost)
            case _ =>
          }
        }

        if (probability < getPercent) {
          if (numPosts > 50) {
            context.system.scheduler.scheduleOnce(randomDuration(5), self, GetFriendsPost)
          }
        }

        if (probability < friendPercent) {
          context.system.scheduler.scheduleOnce(randomDuration(3), self, MakeFriend)
        }

        if (probability < updatePercent) {
          context.system.scheduler.scheduleOnce(randomDuration(3), self, UpdatePost(status, -1))
          if (numAlbums > 0) {
            context.system.scheduler.scheduleOnce(randomDuration(3), self, AddPictureToAlbum)
          }
        }

        context.system.scheduler.scheduleOnce(randomDuration(3), self, false)
      }

    /*TODO
    case UpdatePicture(pictureID) =>

    case UpdateAlbum(albumID) =>

    case UpdateSelf =>

    case UpdatePost(postType, attachmentID) =>

     */

    case MakePost(postType, attachmentID) =>
      val newPost = Objects.Post(baseObject = BaseObject(), myBaseObj.id, new DateTime().toString(), myBaseObj.id, statuses(Random.nextInt(statuses.length)), postType, attachmentID)
      put(newPost.toJson.asJsObject, "post")
    case MakePicturePost =>
      val newPicture = Picture(BaseObject(), myBaseObj.id, -1, "filename.png", "blah")
      put(newPicture.toJson.asJsObject, "picture", "picturePost")
    case MakePicture(albumID) =>
      val newPicture = Picture(BaseObject(), myBaseObj.id, albumID, "filename.png", "blah")
      put(newPicture.toJson.asJsObject, "picture")
    case AddPictureToAlbum =>
      get(s"album/${myBaseObj.id}", "getalbumaddpicture")
    case MakeAlbum =>
      val newAlbum = Album(baseObject = BaseObject(), myBaseObj.id, new DateTime().toString(), new DateTime().toString(), -1, "My new Album")
      put(newAlbum.toJson.asJsObject, "album")
    // From matchmaker
    case aNewFriend: ActorRef =>
      log.info(myBaseObj.id + " just met someone")
      aNewFriend ! Handshake(trueBool, myBaseObj.id)
    // From new friend
    case Handshake(needResponse, id) =>
      myRealFriends.put(sender, id)
      self ! MakeFriend(id)
      if (needResponse) {
        sender ! Handshake(falseBool, myBaseObj.id)
      }

    /*TODO check this*/
    case MakeFriend(id) =>
      if (!isPage) {
        val friendId = if (id == -1) Random.nextInt(myBaseObj.id) else id
        if (PageMap.obj(friendId)) {
          putRoute(s"like/$friendId/page/-1/${myBaseObj.id}", "likepage")
        } else {
          val updatedList = UpdateFriendList(myBaseObj.id, friendId)
          post(updatedList.toJson.asJsObject, "addfriend")
        }
      }
    case GetFriendsPost =>
      var friendId = Random.nextInt(myBaseObj.id)
      if (myFriends.nonEmpty) {
        friendId = myFriends(Random.nextInt(myFriends.length))
      }
      get(s"post/$friendId", "getfriendpost")
    case PutMsg(response, reaction) =>
      reaction match {
        case "user" =>
          me = response ~> unmarshal[User]
          myBaseObj = me.baseObject
          context.system.scheduler.scheduleOnce(durationSeconds(10), self, false)
          PageMap.obj.put(myBaseObj.id, isPage)
          log.info("Printing {}", me)
        case "page" =>
          mePage = response ~> unmarshal[Page]
          myBaseObj = mePage.baseObject
          context.system.scheduler.scheduleOnce(durationSeconds(10), self, false)
          PageMap.obj.put(myBaseObj.id, isPage)
          log.info("Printing {}", mePage)
        case "post" =>
          numPosts += 1
        //          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakePost)
        case "album" =>
          numAlbums += 1
        //          context.system.scheduler.scheduleOnce(Random.nextInt(5) second, self, MakeAlbum)
        case "picturePost" =>
          val picture = response ~> unmarshal[Picture]
          context.system.scheduler.scheduleOnce(durationSeconds(1), self, MakePost(photo, picture.baseObject.id))
        case "picture" =>
        //          context.system.scheduler.scheduleOnce(randomDuration(5), self, MakePicture)
        case "likepage"=>
      }
    case GetMsg(response, reaction) =>
      reaction match {
        case "user" =>
        case "page" =>
        case "getalbumaddpicture" =>
          val album = response ~> unmarshal[Album]
          context.system.scheduler.scheduleOnce(randomDuration(5), self, MakePicture(album.baseObject.id))
        case "getfriendpost" =>
        //            context.system.scheduler.scheduleOnce(randomDuration(5), self, GetFriendsPost)
      }
    case PostMsg(response, reaction) =>
      try {
        reaction match {
          case "addfriend" =>
            val updFL = response ~> unmarshal[UpdateFriendList]
            myFriends.append(updFL.fid)
          //          log.info("added friend {}", updFL)
          //          context.system.scheduler.scheduleOnce(randomDuration(5), self, MakeFriend)
        }
      } catch {
        case e: Throwable => log.error(e, "Error for response {}", response)
      }
  }

  def registerMyself() {
    if (isPage) {
      val newPage = Page(BaseObject(), "about", pageCategories(Random.nextInt(pageCategories.length)), -1)
      put(newPage.toJson.asJsObject, "page")
    } else {
      val fullName = Resources.names(Random.nextInt(Resources.names.length)).split(' ')
      val gender = Random.nextInt(2)
      val newUser = User(BaseObject(), "about me", Resources.randomBirthday(), if (gender == 0) 'M' else 'F', fullName(1), fullName(0))
      put(newUser.toJson.asJsObject, "user")
    }
  }


  def putRoute(route: String, returnSupport: String = ""):Unit = {
    val returnClass = if (returnSupport.nonEmpty) returnSupport else route
    val pipeline = sendReceive

    val future = pipeline {
      pipelining.Put(s"http://$serviceHost:$servicePort/$route")
    }

    future onComplete {
      case Success(response) => self ! PutMsg(response, returnClass)
      case Failure(error) => log.error(error, s"Couldn't create using $route")
    }
  }

  def put(json: JsObject, route: String, returnSupport: String = ""):Unit = {
    val returnClass = if (returnSupport.nonEmpty) returnSupport else route
    val pipeline = sendReceive

    val future = pipeline {
      pipelining.Put(s"http://$serviceHost:$servicePort/$route", json)
    }

    future onComplete {
      case Success(response) => self ! PutMsg(response, returnClass)
      case Failure(error) => log.error(error, s"Couldn't create $json using $route")
    }
  }

  def get(route: String, returnSupport: String = "") = {
    val returnClass = if (returnSupport.nonEmpty) returnSupport else route
    val pipeline = sendReceive

    val future = pipeline {
      pipelining.Get(s"http://$serviceHost:$servicePort/$route")
    }

    future onComplete {
      case Success(response) => self ! GetMsg(response, returnClass)
      case Failure(error) => log.error(error, s"Couldn't get $route")
    }
  }

  def post(json: JsObject, route: String, returnSupport: String = "") = {
    val returnClass = if (returnSupport.nonEmpty) returnSupport else route
    val pipeline = sendReceive

    val future = pipeline {
      pipelining.Post(s"http://$serviceHost:$servicePort/$route", json)
    }

    future onComplete {
      case Success(response) => self ! PostMsg(response, returnClass)
      case Failure(error) => log.error(error, s"Couldn't post $json using $route")
    }
  }
}