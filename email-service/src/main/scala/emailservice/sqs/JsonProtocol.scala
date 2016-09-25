package emailservice.sqs

import play.api.libs.json.{Reads, _}
import play.api.libs.functional.syntax._

object JsonProtocol extends JsonProtocol

trait JsonProtocol {
  implicit val reads: Reads[SNSEnvelope] = (
    (__ \ "Subject").read[String] and
      (__ \ "Message").read[String]
  )(SNSEnvelope)
}
