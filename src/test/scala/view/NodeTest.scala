package view

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalatags.Text.all._
import scalatags.Text.tags2.{title, _}

class NodeTest extends AnyWordSpec with Matchers with OptionValues {
  "Node.from" should {
    "build a node tree from a scala-tags tree" in {
      Node.from(body("test")).value shouldBe HtmlNode(
        "body",
        Map.empty,
        Seq(TextNode("test"))
      )

      val inTree = html(
        head(
          title("some-title")
        ),
        body(
          main("some-text")
        )
      )
      val outTree = HtmlNode(
        "html",
        Map.empty,
        Seq(
          HtmlNode(
            "head",
            Map.empty,
            Seq(
              HtmlNode("title", Map.empty, Seq(TextNode("some-title")))
            )
          ),
          HtmlNode(
            "body",
            Map.empty,
            Seq(
              HtmlNode("main", Map.empty, Seq(TextNode("some-text")))
            )
          )
        )
      )

      Node.from(inTree).value shouldBe outTree
    }
  }
}
