package model

object TimeEntryStatus {
  case object New      extends TimeEntryStatus
  case object Approved extends TimeEntryStatus
  case object Declined extends TimeEntryStatus
}

sealed trait TimeEntryStatus
