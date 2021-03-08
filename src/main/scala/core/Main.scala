package core

import core.DiffMatchPatch.{Diff, Patch}
import scalatags.Text.all._

import scala.jdk.CollectionConverters.CollectionHasAsScala

sealed trait Node {
  val childNodes: Seq[Node]
}

case class HtmlNode(tag: String, attributes: Map[String, String], childNodes: Seq[Node]) extends Node

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
          ))
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

case class TextChange(element: String, baseIndex: String, sourceIndex: String, patch: List[Diff]) extends Change {
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
    val original = render(State("original"))
    val updated = render(State("updated"))

    for {
      o <- original
      u <- updated
    } yield {
      val changes = diff(o, u)
    }
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
        source.childNodes.zip(base.childNodes).zipWithIndex.flatMap{
          case ((srcChild, baseChild), childIndex) => diff(srcChild, baseChild, s"$index>$childIndex")
        }
    }
  }

  //  def diff(source: Node, base: Node, index: Int = 0) = {
  //    var actions = scala.collection.mutable.Seq.empty
  //
  //    if (source.nodeType == base.nodeType && source.nodeType.contains(TextNode)) {
  //      val srcStr = source.render
  //      val baseStr = base.render
  //      if (srcStr != baseStr) {
  //        val diff = stringDiff.diff_main(srcStr, baseStr).asScala.toList
  //        // TODO: push to actions seq
  //      }
  //
  //      // TODO: return here, we are at a leaf node
  //    }
  //
  //
  //  }

  //  def diffString(source: String, base: String, index: Int, baseElement: Node): Unit = {
  //    import scala.collection._
  //
  //    var o = if (base.isEmpty) mutable.ArrayBuffer() else base.split("\\s+").to(mutable.ArrayBuffer)
  //    var n = if (source.isEmpty) mutable.ArrayBuffer() else source.split("\\s+").to(mutable.ArrayBuffer)
  //
  //    var ns = mutable.Map.empty[String, Map[String, mutable.Buffer[Int]]]
  //    var os = mutable.Map.empty[String, Map[String, mutable.Buffer[Int]]]
  //
  //    for ((n_i, i) <- n.zipWithIndex) {
  //      if (!ns.contains(n_i)) {
  //        ns.update(n_i, Map(
  //          "rows" -> mutable.Buffer.empty,
  //          "o" -> null
  //        ))
  //      }
  //
  //      ns(n_i)("rows").addOne(i)
  //    }
  //
  //    for ((o_i, i) <- o.zipWithIndex) {
  //      if (!os.contains(o_i)) {
  //        os.update(o_i, Map(
  //          "rows" -> mutable.Buffer.empty,
  //          "o" -> null
  //        ))
  //      }
  //
  //      os(o_i)("rows").addOne(i)
  //    }
  //
  //    var N = ArrayBuffer.fill(n.length)(Map.empty[String, String])
  //    var O = ArrayBuffer.fill(o.length)(Map.empty[String, String])
  //
  //    for ((i, ns_i) <- ns) {
  //      val os_i = os(i)
  //      if (ns_i("rows").length == 1 && os.contains(i) && os_i("rows").length == 1) {
  //        N.update(ns_i("rows").head, Map(
  //          "text" -> n(ns_i("rows").head),
  //          "row" -> os_i("rows").head.toString
  //        ))
  //        O.update(os_i("row").head, Map(
  //          "text" -> o(os_i("rows").head),
  //          "row" -> ns_i("rows").head.toString
  //        ))
  //      }
  //    }
  //
  //    for ((n_i, i) <- N.zipWithIndex.dropRight(1)) {
  //      if (
  //        n_i.contains("text") &&
  //          N.lift(i + 1).exists(_.contains("text")) &&
  //          n_i("row").toInt + 1 < O.length &&
  //          O.lift(n_i("row").toInt + 1).exists(_.contains("text")) &&
  //          N.lift(i + 1) == O.lift(n_i("row").toInt + 1)
  //      ) {
  //        N.update(i + 1, Map(
  //          "text" -> n(i + 1),
  //          "row" -> (n_i("row").toInt + 1).toString
  //        ))
  //        O.update(n_i("row").toInt + 1, Map(
  //          "text" -> o(n_i("row").toInt + 1),
  //          "row" -> (i + 1).toString
  //        ))
  //      }
  //    }
  //
  //    /*
  //            if (n[i].text != null && n[i - 1].text == null && n[i].row > 0 && o[n[i].row - 1].text == null &&
  //            n[i - 1] == o[n[i].row - 1]) {
  //            n[i - 1] = {
  //                text: n[i - 1],
  //                row: n[i].row - 1
  //            };
  //            o[n[i].row - 1] = {
  //                text: o[n[i].row - 1],
  //                row: i - 1
  //            };
  //        }
  //    */
  //    for ((n_i, i) <- N.zipWithIndex.tail.reverse) {
  //      if (
  //        n_i.contains("text") &&
  //          N.lift(i - 1).exists(_.contains("text")) &&
  //          n_i("row").toInt > 0 &&
  //          O.lift(n_i("row").toInt - 1).exists(_.contains("text")) &&
  //          N.lift(i - 1) == O.lift(n_i("row").toInt - 1)
  //      ) {
  //        N.update(i - 1, Map(
  //          "text" -> n(i - 1),
  //          "row" -> (n_i("row").toInt - 1).toString
  //        ))
  //        O.update(n_i("row").toInt - 1, Map(
  //          "text" -> o(n_i("row").toInt - 1),
  //          "row" -> (i - 1).toString
  //        ))
  //      }
  //    }
  //
  //  }
}
