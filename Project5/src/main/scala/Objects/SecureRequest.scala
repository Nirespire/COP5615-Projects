package Objects

case class SecureRequest(
                          from: Int,
                          to: Int,
                          getType: String,
                          getIdx: Int
                        )