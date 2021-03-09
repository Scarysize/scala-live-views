package core

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import com.github.plokhotnyuk.jsoniter_scala.core.writeToString
import diff.{Reconcile, TextChange}
import scalatags.Text
import scalatags.Text.all
import scalatags.Text.all._
import view._

import scala.concurrent.duration._

trait ScalaTagsMarshalling {
  implicit val toStringMarshaller: ToEntityMarshaller[Text.TypedTag[String]] =
    Marshaller
      .withFixedContentType(ContentTypes.`text/html(UTF-8)`)(tags =>
        HttpEntity(ContentTypes.`text/html(UTF-8)`, tags.toString())
      )
}

class LiveEndpoint
    extends akka.http.scaladsl.marshalling.sse.EventStreamMarshalling
    with ScalaTagsMarshalling {
  private val source = Source
    .fromIterator(() => Iterator.from(100, 1))
    .throttle(1, 100.millis)
    .map { tick =>
      (for {
        sourceView <- lastView
        baseView <- Option(render(tick.toString))
        source <- Node.from(sourceView)
        base <- Node.from(baseView)
      } yield {
        lastView = Option(baseView)
        val diff = Reconcile.diff(source, base)
        diff
      }).getOrElse(Seq.empty)
    }
    .map(_.collect { case t: TextChange => t })
    .map(change => ServerSentEvent(writeToString(change), "change"))
    .keepAlive(1.second, () => ServerSentEvent.heartbeat)
  private var lastView = Option.empty[Text.TypedTag[String]]

  def route: Route = pathPrefix("live-demo") {
    concat(
      path("updates") {
        get {
          complete(source)
        }
      },
      pathEndOrSingleSlash {
        get {
          lastView = Option(render("tick"))
          complete(lastView.get)
        }
      }
    )
  }

  private def render(state: String) = {
    val view = html(
      all.head(
        script(src := "/assets/live-view.js")
      ),
      body(
        header(
          h1("live view")
        ),
        p(state)
      )
    )
    view
  }
}
