package model.eventadapters

import akka.actor.ExtendedActorSystem
import akka.persistence.journal.{EventAdapter, EventSeq}
import reactivemongo.bson._
import showcase.events.{TimeEntryApproved, TimeEntryCreated, TimeEntryDeclined}
import util.BSONHandlers._

class MigrationEventAdapter(system: ExtendedActorSystem) extends EventAdapter {

  implicit val timeEntryCreatedHandler  = Macros.handler[TimeEntryCreated]
  implicit val timeEntryApprovedHandler = Macros.handler[TimeEntryApproved]
  implicit val timeEntryDeclinedHandler = Macros.handler[TimeEntryDeclined]

  override def fromJournal(event: Any, manifest: String): EventSeq = {
    val e = manifest match {
      case "time_entry.created" =>
        BSON.read[BSONDocument, TimeEntryCreated](event.asInstanceOf[BSONDocument])
      case "time_entry.approved" =>
        BSON.read[BSONDocument, TimeEntryApproved](event.asInstanceOf[BSONDocument])
      case "time_entry.declined" =>
        BSON.read[BSONDocument, TimeEntryDeclined](event.asInstanceOf[BSONDocument])
    }
    EventSeq(e)
  }

  override def manifest(event: Any): String = event match {
    case e: TimeEntryCreated => "time_entry.created"
    case e: TimeEntryApproved => "time_entry.approved"
    case e: TimeEntryDeclined => "time_entry.declined"
  }

  override def toJournal(event: Any): Any = event match {
    case e: TimeEntryCreated  => BSON.write(e)
    case e: TimeEntryApproved => BSON.write(e)
    case e: TimeEntryDeclined => BSON.write(e)
  }
}
