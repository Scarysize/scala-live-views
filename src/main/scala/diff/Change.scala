package diff

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import diff.StringDiff.TextDiff

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
    patch: List[TextDiff]
) extends Change {
  val action = "TEXT_CHANGE"
}

object Change {
  implicit val textChangeCodec: JsonValueCodec[TextChange] =
    JsonCodecMaker.make[TextChange]
  implicit val seqTextChangeCodec: JsonValueCodec[Seq[TextChange]] =
    JsonCodecMaker.make[Seq[TextChange]]
}
