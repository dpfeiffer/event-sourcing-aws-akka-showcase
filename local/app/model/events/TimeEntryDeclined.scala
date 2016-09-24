package model.events

import java.util.UUID

import model.Event
import org.joda.time.DateTime

case class TimeEntryDeclined (
  id: UUID,
  userId: UUID,
  dateTime: DateTime
)extends Event
