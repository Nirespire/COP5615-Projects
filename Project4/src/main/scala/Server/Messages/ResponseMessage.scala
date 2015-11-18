package Server.Messages

case class ResponseMessage(message: String) {
  override def toString(): String = {
    message
  }
}
