package showcase.emailservice

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}
import showcase.emailservice.sqs.SNSEnvelope
import showcase.events.{TimeEntryApproved, TimeEntryCreated, TimeEntryDeclined}

object JsonProtocol extends JsonProtocol

trait JsonProtocol {
  implicit val snsEnvelopeReads: Reads[SNSEnvelope] = (
    (__ \ "Subject").read[String] and
      (__ \ "Message").read[String]
  )(SNSEnvelope)

  implicit val timeEntryCreatedReads    = Json.reads[TimeEntryCreated]
  implicit val timeEntryCreatedApproved = Json.reads[TimeEntryApproved]
  implicit val timeEntryCreatedDeclined = Json.reads[TimeEntryDeclined]
}
