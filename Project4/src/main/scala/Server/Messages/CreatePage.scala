package Server.Messages

import Objects.Page
import spray.routing.RequestContext

case class CreatePage(requestContext: RequestContext, page: Page)
