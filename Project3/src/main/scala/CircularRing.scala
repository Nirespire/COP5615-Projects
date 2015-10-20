object CircularRing {

  def inbetween(startIdx: Int, ipTestIdx: Int, ipStopIdx: Int, hashSpace: Int) = {
    val stopIdx = if (startIdx > ipStopIdx) {
      ipStopIdx + hashSpace
    } else {
      ipStopIdx
    }

    val testIdx = if (ipTestIdx < ipStopIdx && startIdx > ipStopIdx) {
      ipTestIdx + hashSpace
    } else {
      ipTestIdx
    }

    testIdx < stopIdx && testIdx >= startIdx
  }
}