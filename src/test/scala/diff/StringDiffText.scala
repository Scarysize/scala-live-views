package diff

import diff.StringDiff.TextDiff
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StringDiffText extends AnyWordSpec with Matchers {
  "StringDiff.diff" should {
    "return no diffs if the strings are equal" in {
      StringDiff.diff("foo", "foo") shouldBe empty
    }

    "calculate the diff of two strings" in {
      val diffs = StringDiff.diff("hello eric", "hallo erik")
      diffs shouldBe List(
        TextDiff("EQUAL", "h"),
        TextDiff("DELETE", "e"),
        TextDiff("INSERT", "a"),
        TextDiff("EQUAL", "llo eri"),
        TextDiff("DELETE", "c"),
        TextDiff("INSERT", "k")
      )
    }
  }
}
