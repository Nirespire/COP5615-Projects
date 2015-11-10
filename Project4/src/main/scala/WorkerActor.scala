import akka.actor.Actor
import spray.routing.RequestContext


class WorkerActor (requestContext: RequestContext) extends Actor{
  def receive = {
    case profileId:Integer =>
      println("receive!")
      requestContext.complete("GET profile " + profileId)
      context.stop(self)
  }

}
