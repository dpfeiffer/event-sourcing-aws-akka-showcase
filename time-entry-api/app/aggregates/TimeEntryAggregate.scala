package aggregates

import java.util.UUID

import aggregates.TimeEntryAggregate.{ApproveTimeEntry, CreateTimeEntry, DeclineTimeEntry, TimeEntryState}
import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import cats.data.{Validated, Xor}
import cats.data.Validated.{Invalid, Valid}
import model.Fail.{InvalidCommand, TimeEntryAlreadyExists, TimeEntryDoesNotExist}
import model.TimeEntryStatus.{Approved, Declined, New}
import model._
import org.joda.time.DateTime
import showcase.events.{Event, TimeEntryApproved, TimeEntryCreated, TimeEntryDeclined}

object TimeEntryAggregate {
  def props(id: UUID) = Props(classOf[TimeEntryAggregate], id)

  case class TimeEntryState(status: TimeEntryStatus)

  case class CreateTimeEntry(id: UUID, begin: DateTime, end: DateTime, timeEntryUserId: UUID, userId: UUID) extends Command
  case class ApproveTimeEntry(id: UUID, userId: UUID)                                                       extends Command
  case class DeclineTimeEntry(id: UUID, userId: UUID)                                                       extends Command
}

class TimeEntryAggregate(id: UUID) extends PersistentActor with ActorLogging {

  var state: TimeEntryState = _

  override def receiveRecover: Receive = {
    case c: TimeEntryCreated =>
      state = TimeEntryState(New)
    case c: TimeEntryApproved =>
      state = state.copy(Approved)
    case c: TimeEntryDeclined =>
      state = state.copy(Declined)
    case RecoveryCompleted => log.debug("Recovered successfully")
  }

  override def receiveCommand: Receive = {
    case c: CreateTimeEntry  => create(c)
    case c: ApproveTimeEntry => approve(c)
    case c: DeclineTimeEntry => decline(c)
  }

  def decline(c: DeclineTimeEntry) = {
    val validation = for {
      _ <- validateCreated.right
      _ <- validateNotDeclined.right
    } yield Unit

    validation.fold(respondError, _ => {
      val event = TimeEntryDeclined(id, c.userId, DateTime.now)
      persist(event) { e =>
        state = state.copy(Declined)
        respondSuccess(e)
      }
    })
  }

  def approve(c: ApproveTimeEntry) = {
    val validation = for {
      _ <- validateCreated.right
      _ <- validateNotApproved.right
    } yield Unit

    validation.fold(respondError, _ => {
      val event = TimeEntryApproved(id, c.userId, DateTime.now)
      persist(event) { e =>
        state = state.copy(status = Approved)
        respondSuccess(e)
      }
    })
  }

  def create(c: CreateTimeEntry) = {
    val validation = validateNewTimeEntry

    validation.fold(respondError, _ => {
      val event = TimeEntryCreated(id, c.begin, c.end, c.timeEntryUserId, c.userId, DateTime.now)
      persist(event) { e =>
        state = TimeEntryState(New)
        respondSuccess(e)
      }
    })
  }

  def respondError(f: Fail) = {
    sender() ! Left[Fail, Seq[Event]](f)
  }

  def respondSuccess[E <: Event](e: E) = {
    sender() ! Right[Fail, Seq[Event]](Seq(e))
  }

  def validateNewTimeEntry: Either[Fail, Unit] = {
    if (lastSequenceNr == 0) Right(()) else Left(TimeEntryAlreadyExists(id))
  }

  def validateCreated: Either[Fail, Unit] = {
    if (lastSequenceNr > 0) Right(()) else Left(TimeEntryDoesNotExist(id))
  }

  def validateNotApproved: Either[Fail, Unit] = {
    if (state.status == Approved) Left(InvalidCommand)
    else Right(())
  }

  def validateNotDeclined: Either[Fail, Unit] = {
    if (state.status == Declined) Left(InvalidCommand)
    else Right(())
  }

  override def persistenceId: String = s"time-entry-$id"
}
