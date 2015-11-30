package Server.Actors

import Utils.Constants

import scala.collection.mutable

case class DebugInfo(debugVar: scala.collection.mutable.Map[Char, Int] =
                     mutable.HashMap[Char, Int]().withDefaultValue(0)) {
  val start = System.nanoTime()

  def putRequestPerSecond() = (debugVar(Constants.putProfilesChar) +
    debugVar(Constants.putAlbumsChar) +
    debugVar(Constants.postFlChar) +
    debugVar(Constants.putPicturesChar) +
    debugVar(Constants.putPostsChar)
    ) * Constants.nano / (System.nanoTime() - start)

  def getRequestPerSecond() = (debugVar(Constants.getProfilesChar) +
    debugVar(Constants.getAlbumsChar) +
    debugVar(Constants.getFlChar) +
    debugVar(Constants.getPicturesChar) +
    debugVar(Constants.getPostsChar) +
    debugVar(Constants.getFeedChar)
    ) * Constants.nano / (System.nanoTime() - start)

  def allRequestPerSecond() = (debugVar(Constants.putProfilesChar) +
    debugVar(Constants.putAlbumsChar) +
    debugVar(Constants.postFlChar) +
    debugVar(Constants.putPicturesChar) +
    debugVar(Constants.putPostsChar) +
    debugVar(Constants.getProfilesChar) +
    debugVar(Constants.getAlbumsChar) +
    debugVar(Constants.getFlChar) +
    debugVar(Constants.getPicturesChar) +
    debugVar(Constants.getPostsChar) +
    debugVar(Constants.getFeedChar) +
    debugVar(Constants.likeChar)
    ) * Constants.nano / (System.nanoTime() - start)
}