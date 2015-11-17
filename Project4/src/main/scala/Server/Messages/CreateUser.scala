package Server.Messages

import Objects.User
import spray.routing.RequestContext

case class CreateUser(requestContext: RequestContext, user: User)
