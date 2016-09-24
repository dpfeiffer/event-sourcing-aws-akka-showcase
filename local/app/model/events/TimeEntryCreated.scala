package model.events

import java.util.UUID

import model.Event
import org.joda.time.DateTime

case class TimeEntryCreated(
    id: UUID,
    begin: DateTime,
    end: DateTime,
    timeEntryUserId: UUID,
    userId: UUID,
    dateTime: DateTime
) extends Event
