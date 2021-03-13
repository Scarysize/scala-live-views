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

case class TextChange(
    path: String,
    patch: List[TextDiff]
) extends Change {
  val action = "TEXT_CHANGE"
}

case class AppendChild(
    path: String,
    nodeToInsert: Node
) extends Change {
  val action = "APPEND_CHILD"
}

case class ReplaceChild(
    path: String,
    nodeToInsert: Node
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
