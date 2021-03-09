package view

import scalatags.Text.all.{ConcreteHtmlTag, Modifier, StringFrag}

sealed trait Node {
  val childNodes: Seq[Node]
}

case class HtmlNode(
                     tag: String,
                     attributes: Map[String, String],
                     childNodes: Seq[Node]
                   ) extends Node

case class TextNode(textContent: String) extends Node {
  override val childNodes: Seq[Node] = Seq.empty
}

object Node {
  def from(modifier: Modifier): Option[Node] = {
    val node = modifier match {
      case stringFrag: StringFrag =>
        Option(TextNode(stringFrag.render))
      case htmlTag: ConcreteHtmlTag[String] =>
        Option(
          HtmlNode(
            htmlTag.tag,
            Map.empty,
            childNodes = htmlTag.modifiers.flatMap(_.flatMap(Node.from))
          )
        )
      case _ =>
        Option.empty[Node]
    }

    node
  }
}
