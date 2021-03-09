package diff

import view._

object Reconcile {
  def diff(source: Node, base: Node): Seq[Change] = {
    diff(source, base, "0")
  }

  private def diff(source: Node, base: Node, index: String): Seq[Change] = {
    (source, base) match {
      case (s: TextNode, b: TextNode) if s.textContent != b.textContent =>
        val textDiffs = StringDiff.diff(s.textContent, b.textContent)
        Seq(TextChange("", index, index, textDiffs))
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
