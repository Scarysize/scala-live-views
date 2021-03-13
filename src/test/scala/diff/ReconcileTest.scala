package diff

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import view.{HtmlNode, Node, TextNode}

class ReconcileTest extends AnyWordSpec with Matchers {
  def h(tag: String, nodes: Node*): Node = {
    HtmlNode(tag, Map.empty, nodes)
  }

  def t(text: String): Node = TextNode(text)

  "Reconcile.diff" should {
    "not detect changes in equal trees" in {
      val source = h(
        "div",
        h("p", t("foo")),
        h("p", t("bar"))
      )
      val target = h(
        "div",
        h("p", t("foo")),
        h("p", t("bar"))
      )
      Reconcile.diff(source, target) shouldBe empty
    }

    "detect text changes" in {
      val change :: Nil = Reconcile
        .diff(h("div", t("hello eric")), h("div", t("hallo erik")))
        .toList
      val txtChange = change.asInstanceOf[TextChange]
      txtChange.path shouldBe "0>0"
    }

    "detect nested text changes" in {
      val source = h(
        "div",
        h("p", t("fix")),
        h("p", t("test"))
      )
      val target = h(
        "div",
        h("p", t("fix")),
        h("p", t("text"))
      )
      val change :: Nil = Reconcile.diff(source, target).toList
      val txtChange = change.asInstanceOf[TextChange]

      txtChange.patch.map(_.text) shouldBe List("te", "s", "x", "t")
      txtChange.path shouldBe "0>1>0"
    }

    "compare child nodes with on the same index" in {
      val source = h("div", h("span"))
      val target = h("div", h("a"), h("span"))

      val changes = Reconcile.diff(source, target)
      changes.length shouldBe 2
      changes.map(_.action) shouldBe List("REPLACE_CHILD", "APPEND_CHILD")

      val replace :: append :: Nil = changes
      replace.asInstanceOf[ReplaceChild].replacement shouldBe h("a")
      replace.asInstanceOf[ReplaceChild].path shouldBe "0>0"

      append.asInstanceOf[AppendChild].appendix shouldBe h("span")
      append.asInstanceOf[AppendChild].path shouldBe "0"
    }
  }
}
