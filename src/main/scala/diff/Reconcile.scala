package diff

import view._

import scala.util.control.Breaks.{break, breakable}

object Reconcile {
  def diff(src: Node, tar: Node, path: String = "0"): Seq[Change] = {
    val instructions = scala.collection.mutable.ArrayBuffer.empty[Change]

    val srcChs = src.childNodes
    val tarChs = tar.childNodes

    for (a <- tarChs.indices) {
      breakable {
        val curSrc = srcChs(a)
        val curTar = tarChs(a)

        if (curSrc == null) {
          instructions.append(
            AppendChild(
              path = path,
              nodeToInsert = curTar
            )
          )
          break
        }

        instructions.appendAll(diffNode(curSrc, curTar, s"$path>${a.toString}"))
      }
    }

    if (tarChs.length < srcChs.length) {
      for (a <- tarChs.length until srcChs.length) {
        instructions.append(
          RemoveChild(
            path = "todo",
            childToRemove = a
          )
        )
      }
    }

    instructions.toSeq
  }

  private def diffNode(src: Node, tar: Node, path: String): Seq[Change] = {
    val instructions = compareNodes(src, tar, path)

    instructions match {
      case Some(is) =>
        is.concat(diff(src, tar, path))
      case None =>
        Seq(
          ReplaceChild(
            path = "",
            nodeToInsert = tar
          )
        )
    }
  }

  private def compareNodes(
      source: Node,
      target: Node,
      path: String
  ): Option[Seq[Change]] = {
    (source, target) match {
      case (s: HtmlNode, t: HtmlNode) if s.tag != t.tag =>
        Option.empty
      case (s: HtmlNode, t: HtmlNode) =>
        // TODO: compare html elements
        Option(Seq.empty)
      case (s: TextNode, t: TextNode) =>
        val stringDiff = StringDiff.diff(s.textContent, t.textContent)
        if (stringDiff.isEmpty) {
          Option(Seq.empty)
        } else {
          Option(Seq(TextChange(path, stringDiff)))
        }
      case _ =>
        Option(Seq.empty)
    }
  }
}
