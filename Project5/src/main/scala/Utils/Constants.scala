package Utils

import java.security.PublicKey

import scala.collection.mutable

object Constants {
  val putProfilesChar = "putProfilesCnt"
  val putPostsChar = "putPostsCnt"
  val putAlbumsChar = "putAlbumsCnt"
  val putPicturesChar = "putPicturesCnt"

  val postAddFriendChar = "postAddFriendCnt"
  val postUserChar = "postUserCnt"
  val postPageChar = "postPageCnt"
  val postPostChar = "postPostCnt"
  val postPictureChar = "postPictureCnt"
  val postAlbumChar = "postAlbumCnt"

  val getProfilesChar = "getProfilesCnt"
  val getPostsChar = "getPostsCnt"
  val getPicturesChar = "getPicturesCnt"
  val getAlbumsChar = "getAlbumsCnt"
  val getAddFriendChar = "getAddFriendCnt"
  val getFeedChar = "getFeedCnt"

  val deleteUserChar = "deleteUserCnt"
  val deletePageChar = "deletePageCnt"
  val deletePostChar = "deletePostCnt"
  val deletePictureChar = "deletePictureCnt"
  val deleteAlbumChar = "deleteAlbumCnt"

  val likeChar = "likeCnt"
  val postLikeChar = "postLikeCnt"

  val registerChar = "registerCnt"

  val nano = 1000000000.0
  val trueBool = true
  val falseBool = false

  val IV = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)

  val randomStringHeader = "RandomString"
  val signedStringHeader = "SignedString"
  val authTokenHeader = "AuthToken"
  val serverPublicKeyHeader = "ServerPublicKey"

  val serverId = -1
  val defaultKeyPair = Crypto.generateRSAKeys()
  val defaultPublicKey = defaultKeyPair.getPublic
  val defaultPrivateKey = defaultKeyPair.getPrivate
  val defaultKey = Crypto.generateAESKey()
  val serverKeyPair = Crypto.generateRSAKeys()
  val serverPublicKey = serverKeyPair.getPublic
  val serverPrivateKey = serverKeyPair.getPrivate

  val userPublicKeys = mutable.HashMap[Int, PublicKey]()
}