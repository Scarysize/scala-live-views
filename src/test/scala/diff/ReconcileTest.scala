package diff

import view.{TextNode, HtmlNode}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ReconcileTest extends AnyWordSpec with Matchers {
  "Reconcile.diff" should {
    "detect text changes" in {
      val change :: Nil =
        Reconcile.diff(TextNode("hello eric"), TextNode("hallo erik"))
      change shouldBe a[TextChange]
      change.baseIndex shouldBe "0"
      change.sourceIndex shouldBe "0"
    }
  }

  "detect nested text changes" in {
    val source = HtmlNode(
      "main",
      Map.empty,
      Seq(
        HtmlNode("p", Map.empty, Seq(TextNode("fix"))),
        HtmlNode("p", Map.empty, Seq(TextNode("test")))
      )
    )
    val base = HtmlNode(
      "main",
      Map.empty,
      Seq(
        HtmlNode("p", Map.empty, Seq(TextNode("fix"))),
        HtmlNode("p", Map.empty, Seq(TextNode("text")))
      )
    )
    val change :: Nil = Reconcile.diff(source, base)
    change shouldBe a[TextChange]
    change.baseIndex shouldBe "0>1>0"
    change.sourceIndex shouldBe "0>1>0"
  }
}
