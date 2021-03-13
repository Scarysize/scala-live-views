package diff

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import view.{HtmlNode, Node, TextNode}

class ReconcileTest extends AnyWordSpec with Matchers {
  def doc(node: Node): Node = {
    HtmlNode("html", Map.empty, Seq(node))
  }

  "Reconcile.diff" should {
    "detect text changes" in {
      val change :: Nil = Reconcile
        .diff(doc(TextNode("hello eric")), doc(TextNode("hallo erik")))
        .toList
      val txtChange = change.asInstanceOf[TextChange]
      txtChange.path shouldBe "0>0"
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
    val change :: Nil = Reconcile.diff(source, base).toList
    val txtChange = change.asInstanceOf[TextChange]

    txtChange.patch.map(_.text) shouldBe List("te", "s", "x", "t")
    txtChange.path shouldBe "0>1>0"
  }
}
