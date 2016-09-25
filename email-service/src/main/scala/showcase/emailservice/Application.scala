package showcase.emailservice

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.libs.json.Json
import showcase.emailservice.JsonProtocol._
import showcase.emailservice.sqs.{SNSEnvelope, SqsSource, SqsSourceSettings}
import showcase.events.{Event, TimeEntryApproved, TimeEntryCreated, TimeEntryDeclined}
object Application {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("email-service")
    implicit val mat    = ActorMaterializer()

    val config   = ConfigFactory.load()
    val settings = config.as[SqsSourceSettings]("sqs")

    val credentials = new BasicAWSCredentials(settings.accessKey, settings.secretKey)
    val sqsClient   = new AmazonSQSAsyncClient(credentials)

    val source = SqsSource(
      settings.queueUrl,
      autoAck = false,
      sqsClient
    )

    source
        .map{msg =>
          Envelope(msg.receiptHandle,msg.body)
        }
        .map{ envelope =>

          val event = for{
            snsEnvelope <- Json.fromJson[SNSEnvelope](Json.parse(envelope.payload))
            event <- snsEnvelope.subject match {
              case "time_entry.created" => Json.fromJson[TimeEntryCreated](Json.parse(snsEnvelope.message))
              case "time_entry.approved" => Json.fromJson[TimeEntryApproved](Json.parse(snsEnvelope.message))
              case "time_entry.declined" => Json.fromJson[TimeEntryDeclined](Json.parse(snsEnvelope.message))
            }
          } yield event

          envelope.copy[Event](payload = event.get)
        }
      .via(sendEmail())
      .runForeach(msg => {
        sqsClient.deleteMessageAsync(settings.queueUrl, msg.receiptHandle)
      })
  }

  def sendEmail(): Flow[Envelope[Event], Envelope[Event], NotUsed] = {
    Flow[Envelope[Event]].map { msg =>
      msg
    }
  }

}
