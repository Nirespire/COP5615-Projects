
import org.scalatest.FunSuite

class CircularRingTest extends FunSuite {
  test("between idx test") {
    assert(CircularRing.inbetween(0, 45, 50, 100))
    assert(CircularRing.inbetween(60, 0, 10, 100))
    assert(!CircularRing.inbetween(10, 0, 60, 100))
    assert(!CircularRing.inbetween(0, 51, 50, 100))
    assert(CircularRing.inbetween(99, 0, 1, 100))
  }

  test("between2 test") {
    assert(CircularRing.inbetween2(1,3,5))
    assert(CircularRing.inbetween2(2,0,2))
  }

}
