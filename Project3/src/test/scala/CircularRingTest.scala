
import org.scalatest.FunSuite

class CircularRingTest extends FunSuite {
  test("inbetweenWithEnds test") {

    // basic circular cases
    assert(OldCircularRing.inbetweenWithEnds(1, 2, 3, 100))
    assert(!OldCircularRing.inbetweenWithEnds(1, 3, 2, 100))
    assert(OldCircularRing.inbetweenWithEnds(3, 1, 2, 100))
    assert(!OldCircularRing.inbetweenWithEnds(3, 2, 1, 100))
    assert(OldCircularRing.inbetweenWithEnds(2, 3, 1, 100))
    assert(!OldCircularRing.inbetweenWithEnds(2, 1, 3, 100))


    // equal values
    assert(!OldCircularRing.inbetweenWithEnds(2, 1, 2, 100))
    assert(OldCircularRing.inbetweenWithEnds(2, 2, 2, 100))

    assert(!OldCircularRing.inbetweenWithEnds(641, 585, 80, 1024))
    assert(OldCircularRing.inbetweenWithEnds(99, 100, 1, 101))
    assert(OldCircularRing.inbetweenWithEnds(99, 0, 1, 101))
    assert(OldCircularRing.inbetweenWithEnds(9, 10, 2, 1024))
    assert(OldCircularRing.inbetweenWithEnds(184, 185, 91, 1024))
    assert(OldCircularRing.inbetweenWithEnds(0, 45, 50, 100))
    assert(!OldCircularRing.inbetweenWithEnds(0, 55, 50, 100))
    assert(OldCircularRing.inbetweenWithEnds(60, 0, 10, 100))
    assert(OldCircularRing.inbetweenWithEnds(60, 70, 10, 100))
    assert(OldCircularRing.inbetweenWithEnds(1, 2, 3, 100))
    assert(!OldCircularRing.inbetweenWithEnds(10, 0, 60, 100))
    assert(!OldCircularRing.inbetweenWithEnds(0, 51, 50, 100))
  }

  test("inbetweenWithoutEnds test") {
    // Basic circular cases
    assert(OldCircularRing.inbetweenWithoutEnds(1, 2, 3, 100))
    assert(!OldCircularRing.inbetweenWithoutEnds(1, 3, 2, 100))
    assert(OldCircularRing.inbetweenWithoutEnds(3, 1, 2, 100))
    assert(!OldCircularRing.inbetweenWithoutEnds(3, 2, 1, 100))
    assert(OldCircularRing.inbetweenWithoutEnds(2, 3, 1, 100))
    assert(!OldCircularRing.inbetweenWithoutEnds(2, 1, 3, 100))

    // equal values
    assert(!OldCircularRing.inbetweenWithoutEnds(2, 1, 2, 100))
    assert(!OldCircularRing.inbetweenWithoutEnds(2, 2, 2, 100))
  }

  test("inbetweenWithoutStart test") {
    // basic circular cases
    assert(OldCircularRing.inbetweenWithoutStart(1, 2, 3, 100))
    assert(!OldCircularRing.inbetweenWithoutStart(1, 3, 2, 100))
    assert(OldCircularRing.inbetweenWithoutStart(3, 1, 2, 100))
    assert(!OldCircularRing.inbetweenWithoutStart(3, 2, 1, 100))
    assert(OldCircularRing.inbetweenWithoutStart(2, 3, 1, 100))
    assert(!OldCircularRing.inbetweenWithoutStart(2, 1, 3, 100))

    // equal values
    assert(!OldCircularRing.inbetweenWithoutStart(2, 1, 2, 100))
    assert(!OldCircularRing.inbetweenWithoutStart(2, 2, 2, 100))
  }

  test("inbetweenWithoutStop test") {
    // basic circular cases
    assert(OldCircularRing.inbetweenWithoutStop(1, 2, 3, 100))
    assert(!OldCircularRing.inbetweenWithoutStop(1, 3, 2, 100))
    assert(OldCircularRing.inbetweenWithoutStop(3, 1, 2, 100))
    assert(!OldCircularRing.inbetweenWithoutStop(3, 2, 1, 100))
    assert(OldCircularRing.inbetweenWithoutStop(2, 3, 1, 100))
    assert(!OldCircularRing.inbetweenWithoutStop(2, 1, 3, 100))

    assert(!OldCircularRing.inbetweenWithoutStop(2, 1, 2, 100))
    assert(OldCircularRing.inbetweenWithoutStop(2, 2, 2, 100))

  }




}
