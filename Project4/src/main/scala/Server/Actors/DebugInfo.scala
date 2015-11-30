package Server.Actors

import Utils.Constants

import scala.collection.mutable

case class DebugInfo(debugVar: scala.collection.mutable.Map[Char, Int] =
                     mutable.HashMap[Char, Int]().withDefaultValue(0)) {
  val start = System.nanoTime()

  def postRequestPerSecond() = (debugVar(Constants.profilesChar) +
    debugVar(Constants.albumsChar) +
    debugVar(Constants.flChar) +
    debugVar(Constants.picturesChar) +
    debugVar(Constants.postsChar)
    ) * Constants.nano / (System.nanoTime() - start)

  def getRequestPerSecond() = (debugVar(Constants.getProfilesChar) +
    debugVar(Constants.getAlbumsChar) +
    debugVar(Constants.getFlChar) +
    debugVar(Constants.getPicturesChar) +
    debugVar(Constants.getPostsChar) +
    debugVar(Constants.getFeedChar)
    ) * Constants.nano / (System.nanoTime() - start)

  def allRequestPerSecond() = (debugVar(Constants.profilesChar) +
    debugVar(Constants.albumsChar) +
    debugVar(Constants.flChar) +
    debugVar(Constants.picturesChar) +
    debugVar(Constants.postsChar) +
    debugVar(Constants.getProfilesChar) +
    debugVar(Constants.getAlbumsChar) +
    debugVar(Constants.getFlChar) +
    debugVar(Constants.getPicturesChar) +
    debugVar(Constants.getPostsChar) +
    debugVar(Constants.getFeedChar)
    ) * Constants.nano / (System.nanoTime() - start)
}