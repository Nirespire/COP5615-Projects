
import java.nio.charset.Charset

import com.google.common.hash.Hashing
import org.scalatest.FunSuite

/**
 * Created by Sanjay on 10/13/2015.
 */
class FingerEntryTest extends FunSuite {
  test("Given two finger entries and a new hash return true if the hash is between them") {
    val opSet = (0 to 1024).foldLeft(Set[Int]()) { case(opSet,value) =>
      val m = 1024
      val hash = Hashing.consistentHash(Hashing.sha256().hashString(value.toString, Charset.forName("UTF-8")), m)
      opSet + hash
    }

    println(opSet.size)
    assert(true)
  }

}
