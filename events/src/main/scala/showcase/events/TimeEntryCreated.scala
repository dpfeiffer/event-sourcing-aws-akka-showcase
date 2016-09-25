package showcase.events

import java.util.UUID

import org.joda.time.DateTime

case class TimeEntryCreated(
    id: UUID,
    begin: DateTime,
    end: DateTime,
    timeEntryUserId: UUID,
    userId: UUID,
    dateTime: DateTime
) extends Event
