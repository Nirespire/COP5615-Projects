package Objects

import akka.actor.Actor


case class Page(
                 id: Int,
                 about: String,
                 category: String,
                 cover: Int,
                 likes: Int
                 ) extends Actor {
  def receive = {
    case _ =>
  }
}