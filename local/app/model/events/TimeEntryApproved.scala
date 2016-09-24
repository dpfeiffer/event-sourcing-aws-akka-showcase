package model.events

import java.util.UUID

import model.Event
import org.joda.time.DateTime

case class TimeEntryApproved(
    id: UUID,
    userId: UUID,
    dateTime: DateTime
) extends Event
