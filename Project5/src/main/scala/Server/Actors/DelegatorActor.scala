package Server.Actors

import java.security.Key

import Objects.ObjectTypes.ObjectType
import Objects.SecureObject
import Server.Messages._
import Utils.{Constants, DebugInfo}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable

class DelegatorActor(serverPublicKey: Key) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Int, ActorRef]()

  def receive = {
    case PutSecureObjMsg(rc, secureObj: SecureObject) => ObjectType(secureObj.objectType) match {
      case ObjectType.user =>
        DebugInfo.debugVar(Constants.putProfilesChar) += 1
        profiles.put(secureObj.to, context.actorOf(Props(new UserActor(secureObj))))
        rc.complete(secureObj.to.toString)
      case ObjectType.page =>
        DebugInfo.debugVar(Constants.putProfilesChar) += 1
        profiles.put(secureObj.to, context.actorOf(Props(new PageActor(secureObj))))
        rc.complete(secureObj.to.toString)
      case _ =>
        ObjectType(secureObj.objectType) match{
          case ObjectType.post => DebugInfo.debugVar(Constants.putPostsChar) += 1
          case ObjectType.picture => DebugInfo.debugVar(Constants.putPicturesChar) += 1
          case ObjectType.album => DebugInfo.debugVar(Constants.putAlbumsChar) += 1
        }
        profiles(secureObj.to) ! PutSecureObjMsg(rc, secureObj)
    }
    case postMsg@PostSecureObjMsg(rc, secureObj) =>
      ObjectType(secureObj.objectType) match{
        case ObjectType.post => DebugInfo.debugVar(Constants.postPostChar) += 1
        case ObjectType.picture => DebugInfo.debugVar(Constants.postPictureChar) += 1
        case ObjectType.album => DebugInfo.debugVar(Constants.postAlbumChar) += 1
      }
      profiles(secureObj.to) ! postMsg
    case delMsg@DeleteSecureObjMsg(rc, secureReq) =>
      ObjectType(secureReq.objectType) match{
        case ObjectType.post => DebugInfo.debugVar(Constants.deletePostChar) += 1
        case ObjectType.picture => DebugInfo.debugVar(Constants.deletePictureChar) += 1
        case ObjectType.album => DebugInfo.debugVar(Constants.deleteAlbumChar) += 1
      }
      profiles(secureReq.to) ! delMsg
    case getMsg@GetSecureObjMsg(rc, secureReq) =>
      ObjectType(secureReq.objectType) match{
        case ObjectType.post => DebugInfo.debugVar(Constants.getPostsChar) += 1
        case ObjectType.picture => DebugInfo.debugVar(Constants.getPicturesChar) += 1
        case ObjectType.album => DebugInfo.debugVar(Constants.getAlbumsChar) += 1
        case ObjectType.user | ObjectType.page => DebugInfo.debugVar(Constants.getProfilesChar) += 1
      }
      profiles(secureReq.to) ! getMsg
    case likeMsg@LikeMsg(rc, secureReq) =>
      DebugInfo.debugVar(Constants.likeChar) += 2
      if (ObjectType(secureReq.objectType) == ObjectType.user) {
        if (profiles.contains(secureReq.from)) profiles(secureReq.from) ! LikeMsg(rc, secureReq)
        if (profiles.contains(secureReq.to)) profiles(secureReq.to) ! likeMsg
      } else {
        profiles(secureReq.to) ! likeMsg
      }
    case getFriendKeysMsg@GetFriendKeysMsg(rc, pid) =>
      DebugInfo.debugVar(Constants.getAddFriendChar) += 1
      profiles(pid) ! getFriendKeysMsg
    case friendReq@GetFriendRequestsMsg(rc, pid) =>
      DebugInfo.debugVar(Constants.postAddFriendChar) += 1
      profiles(pid) ! friendReq
    case addFriend@AddFriendMsg(rc, secureReq) =>
      DebugInfo.debugVar(Constants.postAddFriendChar) += 1
      profiles(secureReq.to) ! addFriend
    case x => log.error(s"Unhandled in DelegatorActor  $x")
  }
}