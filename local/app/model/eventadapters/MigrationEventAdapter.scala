package model.eventadapters

import akka.actor.ExtendedActorSystem
import akka.persistence.journal.{EventAdapter, EventSeq}
import model.events.{TimeEntryApproved, TimeEntryCreated, TimeEntryDeclined}
import reactivemongo.bson._
import util.BSONHandlers._

class MigrationEventAdapter(system: ExtendedActorSystem) extends EventAdapter {

  implicit val timeEntryCreatedHandler = Macros.handler[TimeEntryCreated]
  implicit val timeEntryApprovedHandler = Macros.handler[TimeEntryApproved]
  implicit val timeEntryDeclinedHandler = Macros.handler[TimeEntryDeclined]

  override def fromJournal(event: Any, manifest: String): EventSeq = {
    val e = manifest match {
      case "TimeEntryCreated" =>
        BSON.read[BSONDocument, TimeEntryCreated](event.asInstanceOf[BSONDocument])
      case "TimeEntryApproved" =>
        BSON.read[BSONDocument, TimeEntryApproved](event.asInstanceOf[BSONDocument])
      case "TimeEntryDeclined" =>
        BSON.read[BSONDocument, TimeEntryDeclined](event.asInstanceOf[BSONDocument])
    }
    EventSeq(e)
  }

  override def manifest(event: Any): String = event.getClass.getSimpleName

  override def toJournal(event: Any): Any = event match {
    case e: TimeEntryCreated => BSON.write(e)
    case e: TimeEntryApproved => BSON.write(e)
    case e: TimeEntryDeclined => BSON.write(e)
  }
}
