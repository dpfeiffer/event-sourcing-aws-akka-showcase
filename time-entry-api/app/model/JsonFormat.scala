package model

import play.api.libs.json.Json
trait JsonFormat {

  implicit val timeEntryWrites = Json.writes[TimeEntry]
}
