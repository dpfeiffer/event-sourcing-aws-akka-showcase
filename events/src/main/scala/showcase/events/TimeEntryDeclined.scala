package showcase.events

import java.util.UUID

import org.joda.time.DateTime

case class TimeEntryDeclined (
  id: UUID,
  userId: UUID,
  dateTime: DateTime
)extends Event
