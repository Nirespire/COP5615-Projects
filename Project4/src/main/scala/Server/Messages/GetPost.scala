package Server.Messages

import spray.routing.RequestContext

case class GetPost(requestContext: RequestContext, profileId: Int, postId: Int = -1)