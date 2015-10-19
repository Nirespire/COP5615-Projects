object CircularRing {

  def inbetween(startIdx: Int, ipTestIdx: Int, ipStopIdx: Int, m: Int) = {
    val stopIdx = if (startIdx > ipStopIdx) {
      ipStopIdx + m
    } else {
      ipStopIdx
    }

    val testIdx = if (ipTestIdx < ipStopIdx && startIdx > ipStopIdx) {
      ipTestIdx + m
    } else {
      ipTestIdx
    }

    testIdx < stopIdx && testIdx >= startIdx
  }

  def inbetween2(start: Int, query: Int, end: Int) = {
    if (start == end) {
      true
    }
    else if (start < end) {
      query >= start && query <= end
    }
    else {
      query >= start || query <= end
    }
  }

}
