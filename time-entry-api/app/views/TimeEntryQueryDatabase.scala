package views

import java.util.UUID
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.typesafe.scalalogging.LazyLogging
import model.TimeEntry
import showcase.events.{Event, TimeEntryApproved, TimeEntryCreated, TimeEntryDeclined}

class TimeEntryQueryDatabase @Inject()(implicit system: ActorSystem) extends LazyLogging {

  implicit val mat = ActorMaterializer()

  private var db = Map[UUID, TimeEntry]()

  private val subscriber =
    Flow[Event].to(Sink.foreach(handle)).runWith(Source.actorRef[Event](100, OverflowStrategy.dropNew))

  system.eventStream.subscribe(subscriber, classOf[Event])

  private def handle(event: Event): Unit = event match {
    case TimeEntryCreated(id, begin, end, timeEntryUserId, userId, dateTime) =>
      db = db + (id -> TimeEntry(id, begin, end, timeEntryUserId, userId, "NEW"))
    case TimeEntryApproved(id, userId, dateTime) =>
      for { e <- db.get(id) } {
        db = db.updated(id, e.copy(status = "APPROVED"))
      }
    case TimeEntryDeclined(id, userId, dateTime) =>
      for { e <- db.get(id) } {
        db = db.updated(id, e.copy(status = "DECLINED"))
      }
  }

  def list(userId: UUID): Seq[TimeEntry] = {
    db.values.find(_.timeEntryUserId == userId).toSeq.sortBy(_.begin.getMillis)
  }

}
