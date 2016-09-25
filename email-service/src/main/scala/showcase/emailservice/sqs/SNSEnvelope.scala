package showcase.emailservice.sqs

case class SNSEnvelope(
  subject: String,
  message : String
)