package Server.Actors

case class DebugInfo(var profiles: Int = 0,
                      var users: Int = 0,
                    var pages: Int = 0,
                     var posts: Int = 0,
                     var albums: Int = 0,
                     var  pictures: Int = 0,
                     var friendlistUpdates: Int = 0
                      ) {

  val start = System.nanoTime()
}
