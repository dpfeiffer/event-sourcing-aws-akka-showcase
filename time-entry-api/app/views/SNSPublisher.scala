package views

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sns.AmazonSNSAsyncClient
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.Configuration
import play.api.libs.json.Json
import showcase.events.{Event, TimeEntryApproved, TimeEntryCreated, TimeEntryDeclined}
import views.SNSPublisher.SnsSettings

object SNSPublisher {
  case class SnsSettings(endpoint: String, topicArn: String, accessKey: String, secretKey: String)
}

class SNSPublisher @Inject()(config: Configuration)(implicit system: ActorSystem) {

  implicit val timeEntryCreatedWrites = Json.writes[TimeEntryCreated]
  implicit val timeEntryApproved      = Json.writes[TimeEntryApproved]
  implicit val timeEntryDeclined      = Json.writes[TimeEntryDeclined]
  implicit val mat                    = ActorMaterializer()

  private val settings = config.underlying.as[SnsSettings]("sns")

  val snsClient = {
    val client = new AmazonSNSAsyncClient(new BasicAWSCredentials(settings.accessKey, settings.secretKey))
    client.setEndpoint(settings.endpoint)
    client
  }

  private val subscriber =
    Flow[Event]
      .map({
        case e: TimeEntryCreated  => ("time_entry.created", Json.toJson(e).toString)
        case e: TimeEntryApproved => ("time_entry.approved", Json.toJson(e).toString)
        case e: TimeEntryDeclined => ("time_entry.declined", Json.toJson(e).toString)
      })
      .to(Sink.foreach {
        case (subject, body) =>
          snsClient.publish(settings.topicArn, body, subject)
      })
      .runWith(Source.actorRef[Event](100, OverflowStrategy.dropNew))

  system.eventStream.subscribe(subscriber, classOf[Event])

}
