package core

import core.DiffMatchPatch.Diff
import core.Main.diff
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalatags.Text.all._
import scalatags.Text.tags2.{title, _}

class NodeTest extends AnyWordSpec with Matchers with OptionValues {
  "Node.from" should {
    "build a node tree from a scala-tags tree" in {
      Node.from(body("test")).value shouldBe HtmlNode("body", Map.empty, Seq(TextNode("test")))

      val inTree = html(
        head(
          title("some-title")
        ),
        body(
          main("some-text")
        )
      )
      val outTree = HtmlNode("html", Map.empty, Seq(
        HtmlNode("head", Map.empty, Seq(
          HtmlNode("title", Map.empty, Seq(TextNode("some-title")))
        )),
        HtmlNode("body", Map.empty, Seq(
          HtmlNode("main", Map.empty, Seq(TextNode("some-text")))
        ))
      ))

      Node.from(inTree).value shouldBe outTree
    }
  }

  "diff" should {
    import DiffMatchPatch.Operation._
    "detect text changes" in {
      val change :: Nil = diff(TextNode("hello eric"), TextNode("hallo erik")).toList
      change shouldBe a[TextChange]
      change.baseIndex shouldBe "0"
      change.sourceIndex shouldBe "0"
      change.asInstanceOf[TextChange].patch shouldBe List(
        new Diff(EQUAL,"h"), new Diff(DELETE,"e"), new Diff(INSERT,"a"), new Diff(EQUAL,"llo eri"), new Diff(DELETE,"c"), new Diff(INSERT,"k")
      )
    }

    "detect nested text changes" in {
      val source = HtmlNode("main", Map.empty, Seq(
        HtmlNode("p", Map.empty, Seq(TextNode("fix"))),
        HtmlNode("p", Map.empty, Seq(TextNode("test")))
      ))
      val base = HtmlNode("main", Map.empty, Seq(
        HtmlNode("p", Map.empty, Seq(TextNode("fix"))),
        HtmlNode("p", Map.empty, Seq(TextNode("text")))
      ))
      val change :: Nil = diff(source, base)
      change shouldBe a[TextChange]
      change.baseIndex shouldBe "0>1>0"
      change.sourceIndex shouldBe "0>1>0"
      change.asInstanceOf[TextChange].patch shouldBe List(
        new Diff(EQUAL, "te"), new Diff(DELETE, "s"), new Diff(INSERT, "x"), new Diff(EQUAL, "t")
      )
    }
  }
}
