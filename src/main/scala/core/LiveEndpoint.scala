package core

import scalatags.Text.all._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Source
import scala.concurrent.duration._
import akka.http.scaladsl.model.sse.ServerSentEvent

class LiveEndpoint  extends akka.http.scaladsl.marshalling.sse.EventStreamMarshalling {
  private def render(state: String) = {
    val view = html(
      body(
        header(
          h1("live view")
        ),
        p(state)
      )
    )
    view
  }

  val source = Source
    .tick(0.seconds, 1.second, Iterator(0).next())
    .map(tick => ServerSentEvent(tick.toString))

  def route: Route = path("live") {
    pathEndOrSingleSlash {
      get {
        complete(render("tick").toString())
      }
    } ~
    path("updates") {
      complete(source)
    }
  }
}
