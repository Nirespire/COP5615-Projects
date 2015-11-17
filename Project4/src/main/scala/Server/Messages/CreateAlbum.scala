package Server.Messages

import Objects.Album
import spray.routing.RequestContext

case class CreateAlbum(requestContext: RequestContext, album: Album)
