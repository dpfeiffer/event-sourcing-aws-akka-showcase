package emailservice.sqs

case class Envelope[P](
    receiptHandle: String,
    payload: P
)
