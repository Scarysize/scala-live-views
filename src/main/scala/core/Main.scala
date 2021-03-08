package core

import core.DiffMatchPatch.{Diff, Patch}
import scalatags.Text.all._

import scala.jdk.CollectionConverters.CollectionHasAsScala
import akka.http.scaladsl.Http
import akka.actor.ActorSystem

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
    println(modifier)
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

sealed trait Change {
  val action: String
  val element: String
  val baseIndex: String
  val sourceIndex: String
}

case class TextChange(
    element: String,
    baseIndex: String,
    sourceIndex: String,
    patch: List[Diff]
) extends Change {
  val action = "TEXT_CHANGE"
}

object Main {

  case class State(title: String)

  def render(state: State): Option[Node] = {
    val tree = html(
      body(
        header(state.title)
      )
    )

    Node.from(tree)
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    // val original = render(State("original"))
    // val updated = render(State("updated"))

    // for {
    //   o <- original
    //   u <- updated
    // } yield {
    //   val changes = diff(o, u)
    // }
    val endpoint = new LiveEndpoint()

    Http().newServerAt("localhost", 8080).bind(endpoint.route)
  }

  private val stringDiff = new DiffMatchPatch()

  def diff(source: Node, base: Node, index: String = "0"): Seq[Change] = {
    println(source, base)
    (source, base) match {
      case (s: TextNode, b: TextNode) if s.textContent != b.textContent =>
        val diffResult = stringDiff.diff_main(s.textContent, b.textContent)
        stringDiff.diff_cleanupEfficiency(diffResult)
        val strDiff = diffResult.asScala.toList
        Seq(TextChange("", index, index, strDiff))
      case (_: TextNode, _: TextNode) =>
        Seq.empty
      case _ =>
        source.childNodes.zip(base.childNodes).zipWithIndex.flatMap {
          case ((srcChild, baseChild), childIndex) =>
            diff(srcChild, baseChild, s"$index>$childIndex")
        }
    }
  }
}
