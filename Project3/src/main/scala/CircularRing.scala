object CircularRing {

  def inbetween(start:Int, testIdx:Int, stopIdx:Int, m:Int) ={
    testIdx < stopIdx  &&  (testIdx > start || start > stopIdx)
  }

  def inbetween2(start:Int, query:Int, end:Int) ={
    if(start <= end){
      query >= start && query <= end
    }
    else{
      query >= start || query <= end
    }
  }

}
