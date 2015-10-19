
import org.scalatest.FunSuite

class CircularRingTest extends FunSuite {
  test("between idx test") {
    assert(!CircularRing.inbetween(641, 585, 80, 1024))
    assert(CircularRing.inbetween(99, 100, 1, 101))
    assert(CircularRing.inbetween(99, 0, 1, 101))
    assert(CircularRing.inbetween(9, 10, 2, 1024))
    assert(CircularRing.inbetween(184, 185, 91, 1024))
    assert(CircularRing.inbetween(0, 45, 50, 100))
    assert(!CircularRing.inbetween(0, 55, 50, 100))
    assert(CircularRing.inbetween(60, 0, 10, 100))
    assert(CircularRing.inbetween(60, 70, 10, 100))
    assert(CircularRing.inbetween(1, 2, 3, 100))
    assert(!CircularRing.inbetween(10, 0, 60, 100))
    assert(!CircularRing.inbetween(0, 51, 50, 100))
    //    assert(CircularRing.inbetween(461, 713, 461, 1024))
  }

  test("between2 test") {
    assert(CircularRing.inbetween2(1, 3, 5))
    assert(CircularRing.inbetween2(2, 0, 2))
  }

}
