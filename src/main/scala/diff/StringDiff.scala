package diff

import scala.jdk.CollectionConverters._

object StringDiff {
  private val diffMatchPatch = new DiffMatchPatch()

  def diff(source: String, base: String): List[TextDiff] = {
    val javaDiff = diffMatchPatch.diff_main(source, base)
    diffMatchPatch.diff_cleanupEfficiency(javaDiff)

    javaDiff.asScala
      .map(diff => TextDiff(diff.operation.toString, diff.text))
      .toList
  }

  case class TextDiff(operation: String, text: String)
}
