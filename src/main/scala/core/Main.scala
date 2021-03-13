package core

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val endpoint = new LiveEndpoint()

    Http()
      .newServerAt("localhost", 8080)
      .bind(
        concat(
          pathPrefix("assets")(getFromResourceDirectory("assets")),
          endpoint.route
        )
      )
  }

  case class State(title: String)

}
