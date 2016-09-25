package model

import java.util.UUID

object Fail {
  case object InvalidJson                     extends Fail
  case class TimeEntryDoesNotExist(id: UUID)  extends Fail
  case class TimeEntryAlreadyExists(id: UUID) extends Fail
  case object InvalidCommand                  extends Fail
}

sealed trait Fail
