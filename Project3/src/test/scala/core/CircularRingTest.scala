package core

import org.scalatest.FunSuite

class CircularRingTest extends FunSuite {

  test("inBetweenWithoutStartWithoutStop test") {
    // Basic circular cases
    assert(CircularRing.inBetweenWithoutStartWithoutStop(1, 2, 3))
    assert(!CircularRing.inBetweenWithoutStartWithoutStop(1, 3, 2))
    assert(CircularRing.inBetweenWithoutStartWithoutStop(3, 1, 2))
    assert(!CircularRing.inBetweenWithoutStartWithoutStop(3, 2, 1))
    assert(CircularRing.inBetweenWithoutStartWithoutStop(2, 3, 1))
    assert(!CircularRing.inBetweenWithoutStartWithoutStop(2, 1, 3))

    // equal values
    assert(!CircularRing.inBetweenWithoutStartWithoutStop(2, 1, 2))
    assert(!CircularRing.inBetweenWithoutStartWithoutStop(2, 2, 2))
  }

  test("inBetweenWithoutStartWithStop test") {
    // basic circular cases
    assert(CircularRing.inBetweenWithoutStartWithStop(1, 2, 3))
    assert(!CircularRing.inBetweenWithoutStartWithStop(1, 3, 2))
    assert(CircularRing.inBetweenWithoutStartWithStop(3, 1, 2))
    assert(!CircularRing.inBetweenWithoutStartWithStop(3, 2, 1))
    assert(CircularRing.inBetweenWithoutStartWithStop(2, 3, 1))
    assert(!CircularRing.inBetweenWithoutStartWithStop(2, 1, 3))

    // equal values
    assert(!CircularRing.inBetweenWithoutStartWithStop(2, 1, 2))
    assert(!CircularRing.inBetweenWithoutStartWithStop(2, 2, 2))
  }

  test("inBetweenWithStartWithoutStop test") {
    // basic circular cases
    assert(CircularRing.inBetweenWithStartWithoutStop(1, 2, 3))
    assert(!CircularRing.inBetweenWithStartWithoutStop(1, 3, 2))
    assert(CircularRing.inBetweenWithStartWithoutStop(3, 1, 2))
    assert(!CircularRing.inBetweenWithStartWithoutStop(3, 2, 1))
    assert(CircularRing.inBetweenWithStartWithoutStop(2, 3, 1))
    assert(!CircularRing.inBetweenWithStartWithoutStop(2, 1, 3))

    assert(!CircularRing.inBetweenWithStartWithoutStop(2, 1, 2))
    assert(CircularRing.inBetweenWithStartWithoutStop(2, 2, 2))

  }


}
