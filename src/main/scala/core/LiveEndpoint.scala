package core

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import core.LiveView.LiveViewMarshalling
import scalatags.Text
import scalatags.Text.{all, tags2}
import scalatags.Text.all._

import scala.concurrent.duration.DurationInt

trait ScalaTagsMarshalling {
  implicit val toStringMarshaller: ToEntityMarshaller[Text.TypedTag[String]] =
    Marshaller
      .withFixedContentType(ContentTypes.`text/html(UTF-8)`)(tags =>
        HttpEntity(ContentTypes.`text/html(UTF-8)`, tags.toString())
      )
}

class LiveEndpoint(implicit actorSystem: ActorSystem) extends LiveViewMarshalling[Seq[String]] with ScalaTagsMarshalling {

  // TODO: This should be instantiated for each client and then synced via state (pubsub?)
  private val view = new LiveView[Seq[String]](Seq.empty) {

    override def render(): Text.TypedTag[String] = {
      val view = html(
        all.head(
          script(src := "/assets/live-view.js")
        ),
        body(
          header(
            h1("live view")
          ),
          tags2.main(
            getState.map(p(_)) :_*
          )
        )
      )
      view
    }
  }

  private val _ = Source
    .fromIterator(() => Iterator.from(99, 1))
    .throttle(1, 200.millis)
    .runForeach{ tick =>
      val newState = view.getState.appended(tick.toString).takeRight(20)
      view.setState(newState)
    }

  def route: Route = pathPrefix("live-demo") {
    pathEndOrSingleSlash {
      get {
        complete(200, view)
      }
    }
  }
}
