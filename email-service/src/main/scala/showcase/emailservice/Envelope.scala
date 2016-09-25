package showcase.emailservice

case class Envelope[P](
    receiptHandle: String,
    payload: P
)
