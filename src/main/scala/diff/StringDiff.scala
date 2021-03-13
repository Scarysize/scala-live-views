package diff

import scala.jdk.CollectionConverters._

object StringDiff {
  private val diffMatchPatch = new DiffMatchPatch()

  def diff(source: String, target: String): List[TextDiff] = {
    if (source == target) {
      List.empty
    } else {
      val javaDiff = diffMatchPatch.diff_main(source, target)
      diffMatchPatch.diff_cleanupEfficiency(javaDiff)

      javaDiff.asScala
        .map(diff => TextDiff(diff.operation.toString, diff.text))
        .toList
    }
  }

  case class TextDiff(operation: String, text: String)
}
