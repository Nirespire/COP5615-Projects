object CircularRing {

  def inbetween(start:Int, testIdx:Int, stopIdx:Int, m:Int) ={
    testIdx < stopIdx  &&  (testIdx > start || start > stopIdx)
  }

}
