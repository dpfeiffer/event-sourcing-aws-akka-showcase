package offices

import java.util.UUID
import java.util.concurrent.TimeUnit

import aggregates.TimeEntryAggregate
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import model._

import scala.util.{Failure, Success}

object TimeEntryOfficeActor {
  def props = Props[TimeEntryOfficeActor]
}

class TimeEntryOfficeActor extends Actor with ActorLogging {

  implicit val ec      = context.system.dispatcher
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  var aggregates       = Map[UUID, ActorRef]()

  override def receive: Receive = {
    case c: Command =>
      val result = aggregate(c.id).ask(c).mapTo[CmdResult]

      result.onComplete {
        case Failure(ex) =>
          log.error(ex, "Processing command with aggregate resulted in exception.")
        case Success(Left(error)) =>
          log.warning(s"Processing command resulted in error. message=$error")
        case Success(Right(events)) =>
          log.debug(s"Processing command was successful. events=$events")
      }

      pipe(result).to(sender())
  }

  def aggregate(id: UUID): ActorRef = {
    aggregates.get(id) match {
      case None    => createAggregate(id)
      case Some(x) => x
    }
  }

  def createAggregate(id: UUID): ActorRef = {
    val ref = context.actorOf(TimeEntryAggregate.props(id), s"time-entry-aggregate-$id")
    aggregates = aggregates + (id -> ref)
    ref
  }

}
