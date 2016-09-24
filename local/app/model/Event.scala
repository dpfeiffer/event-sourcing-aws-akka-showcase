package model

import java.util.UUID

import org.joda.time.DateTime

trait Event {
  def id: UUID
  def dateTime: DateTime
}
