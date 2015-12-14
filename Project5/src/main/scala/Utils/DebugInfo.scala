package Utils

import scala.collection.mutable

object DebugInfo {
  val debugVar = mutable.HashMap[String, Int]().withDefaultValue(0)
  val start = System.nanoTime()

  def putRequestPerSecond() = (
    debugVar(Constants.putProfilesChar) +
      debugVar(Constants.putAlbumsChar) +
      debugVar(Constants.putPicturesChar) +
      debugVar(Constants.putPostsChar) +
      debugVar(Constants.postLikeChar)
    ) * Constants.nano / (System.nanoTime() - start)

  def postRequestPerSecond() = (debugVar(Constants.postAddFriendChar) +
    debugVar(Constants.postUserChar) +
    debugVar(Constants.postPageChar) +
    debugVar(Constants.postPictureChar) +
    debugVar(Constants.postPostChar) +
    debugVar(Constants.postAlbumChar)
    ) * Constants.nano / (System.nanoTime() - start)

  def deleteRequestPerSecond() = (debugVar(Constants.deleteUserChar) +
    debugVar(Constants.deletePageChar) +
    debugVar(Constants.deletePostChar) +
    debugVar(Constants.deletePictureChar) +
    debugVar(Constants.deleteAlbumChar)
    ) * Constants.nano / (System.nanoTime() - start)


  def getRequestPerSecond() = (
    debugVar(Constants.getProfilesChar) +
      debugVar(Constants.getAlbumsChar) +
      debugVar(Constants.getAddFriendChar) +
      debugVar(Constants.getPicturesChar) +
      debugVar(Constants.getPostsChar) +
      debugVar(Constants.getFeedChar)
    ) * Constants.nano / (System.nanoTime() - start)

  def allRequestPerSecond() = (debugVar(Constants.putProfilesChar) +
    debugVar(Constants.putAlbumsChar) +
    debugVar(Constants.putPicturesChar) +
    debugVar(Constants.putPostsChar) +
    debugVar(Constants.postAddFriendChar) +
    debugVar(Constants.postUserChar) +
    debugVar(Constants.postPageChar) +
    debugVar(Constants.postPictureChar) +
    debugVar(Constants.postPostChar) +
    debugVar(Constants.postAlbumChar) +
    debugVar(Constants.deleteUserChar) +
    debugVar(Constants.deletePageChar) +
    debugVar(Constants.deletePostChar) +
    debugVar(Constants.deletePictureChar) +
    debugVar(Constants.deleteAlbumChar) +
    debugVar(Constants.getProfilesChar) +
    debugVar(Constants.getAlbumsChar) +
    debugVar(Constants.getAddFriendChar) +
    debugVar(Constants.getPicturesChar) +
    debugVar(Constants.getPostsChar) +
    debugVar(Constants.getFeedChar) +
    debugVar(Constants.postLikeChar)
    ) * Constants.nano / (System.nanoTime() - start)
}