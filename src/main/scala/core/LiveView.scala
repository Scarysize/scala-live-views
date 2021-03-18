package core

import akka.NotUsed
import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MessageEntity}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import com.github.plokhotnyuk.jsoniter_scala.core.writeToString
import core.StateActor.GetState
import diff.{Change, Reconcile}
import scalatags.Text
import view.Node

import scala.concurrent.duration.DurationInt

class StateActor[T] extends Actor {
  override def receive: Receive = idle

  private def idle: Receive = { case state: T =>
    context.become(running(state))
  }

  private def running(state: T): Receive = {
    case newState: T =>
      context.become(running(newState))
    case GetState =>
      sender() ! state
  }
}

object StateActor {
  case object GetState
}

// TODO
abstract class LiveView[T](initialState: T)(implicit system: ActorSystem) {
  private lazy val (queue, changeStream) = Source
    .queue[Seq[Change]](0, OverflowStrategy.dropHead)
    .filter(_.nonEmpty)
    .toMat(BroadcastHub.sink[Seq[Change]])(Keep.both)
    .run()

  private var state = initialState

  // this should be the "first" render call made from the outside
  private var lastView = Node.from(render())

  final def setState(t: T): Unit = {
    // TODO: keep this in an actor
    state = t

    for {
      source <- lastView
      target <- Node.from(render())
    } yield {
      lastView = Option(target)
      println(source, target)
      val diff = Reconcile.diff(source, target)
      queue.offer(diff)
    }
  }

  final def changes(): Source[Seq[Change], NotUsed] = changeStream

  final def getState: T = {
    state
  }

  def render(): Text.TypedTag[String]
}

object LiveView {
  trait LiveViewMarshalling[T] extends EventStreamMarshalling {
    import ContentTypes._

    private val viewToStringMarshaller: ToEntityMarshaller[LiveView[T]] = Marshaller
      .withFixedContentType(`text/html(UTF-8)`) { view =>
        HttpEntity(`text/html(UTF-8)`, view.render().toString())
      }

    private val changeStreamMarshaller: ToEntityMarshaller[LiveView[T]] = toEventStream.compose[LiveView[T]] { view =>
      view
        .changes()
        .map(change => ServerSentEvent(writeToString(change), "change"))
        .keepAlive(10.second, () => ServerSentEvent.heartbeat)
    }

    implicit val liveViewMarshaller: Marshaller[LiveView[T], MessageEntity] =
      Marshaller.oneOf(changeStreamMarshaller, viewToStringMarshaller)
  }
}
