package model

import java.util.UUID

trait Command {
  def id: UUID
  def userId: UUID
}
