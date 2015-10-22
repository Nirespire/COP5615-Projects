object OldCircularRing {

  def inbetweenWithoutEnds(startIdx: Int, ipTestIdx: Int, ipStopIdx: Int, hashSpace: Int) = {
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

    testIdx < stopIdx && testIdx > startIdx
  }

  def inbetweenWithEnds(startIdx: Int, ipTestIdx: Int, ipStopIdx: Int, hashSpace: Int) = {
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

    testIdx <= stopIdx && testIdx >= startIdx
  }


  def inbetweenWithoutStart(startIdx: Int, ipTestIdx: Int, ipStopIdx: Int, hashSpace: Int) = {
    val stopIdx = if (startIdx >= ipStopIdx) {
      ipStopIdx + hashSpace
    } else {
      ipStopIdx
    }

    val testIdx = if (ipTestIdx < ipStopIdx && startIdx > ipStopIdx) {
      ipTestIdx + hashSpace
    } else {
      ipTestIdx
    }

    testIdx <= stopIdx && testIdx > startIdx
  }


  def inbetweenWithoutStop(startIdx: Int, ipTestIdx: Int, ipStopIdx: Int, hashSpace: Int) = {
    val stopIdx = if (startIdx >= ipStopIdx) {
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
