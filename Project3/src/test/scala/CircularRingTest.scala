
import org.scalatest.FunSuite

/**
 * Created by Sanjay on 10/13/2015.
 */
class CircularRingTest extends FunSuite {
  test("between idx test") {
    assert(CircularRing.inbetween(0, 45, 50, 100))
    assert(CircularRing.inbetween(60, 0, 10, 100))
    assert(!CircularRing.inbetween(10, 0, 60, 100))
    assert(!CircularRing.inbetween(0, 51, 50, 100))
    assert(CircularRing.inbetween(99, 0, 1, 100))
  }

}
