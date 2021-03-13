package diff

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import diff.StringDiff.TextDiff
import view.Node

sealed trait Change {
  val action: String

  // A path to the node the change is applied to, e.g. "0>0>1"
  val path: String
}

/** @param path The path to the text node to change.
  * @param patch A list of text changes to apply to the text content.
  */
case class TextChange(
    path: String,
    patch: List[TextDiff]
) extends Change {
  val action = "TEXT_CHANGE"
}

/** -
  * @param path The path to the parent node the child should be appended to.
  * @param appendix The appendix
  */
case class AppendChild(
    path: String,
    appendix: Node
) extends Change {
  val action = "APPEND_CHILD"
}

/** -
  * @param path The path to the child to be replace. The parent index can be deduced.
  * @param replacement The replacement
  */
case class ReplaceChild(
    path: String,
    replacement: Node
) extends Change {
  val action = "REPLACE_CHILD"
}

case class RemoveChild(
    path: String,
    childToRemove: Int
) extends Change {
  val action = "REMOVE_CHILD"
}

object Change {
  implicit val textChangeCodec: JsonValueCodec[TextChange] =
    JsonCodecMaker.make[TextChange]
  implicit val seqTextChangeCodec: JsonValueCodec[Seq[TextChange]] =
    JsonCodecMaker.make[Seq[TextChange]]
}
