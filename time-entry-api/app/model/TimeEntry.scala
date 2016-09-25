package model

import java.util.UUID

import org.joda.time.DateTime

case class TimeEntry(
    id: UUID,
    begin: DateTime,
    end: DateTime,
    timeEntryUserId: UUID,
    createdByUserId: UUID,
    status: String
)
