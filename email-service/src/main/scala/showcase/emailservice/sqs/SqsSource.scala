package showcase.emailservice.sqs

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.amazonaws.services.sqs.model.{Message, ReceiveMessageRequest, ReceiveMessageResult}

import scala.collection.JavaConversions._
import scala.collection.mutable

final case class SqsMessage(messageId: String, receiptHandle: String, md5OfBody: String, body: String)

object SqsSource {
  def apply(settings: SqsSourceSettings): Source[SqsMessage, NotUsed] = apply(settings.queueUrl, settings.autoAck, sqsClient(settings))
  def apply(queueUrl: String, autoAck: Boolean, sqsClient: AmazonSQSAsyncClient): Source[SqsMessage, NotUsed] =
    Source.fromGraph(new SqsSource(queueUrl, autoAck, sqsClient))

  private def sqsClient(settings: SqsSourceSettings) = {
    val client = new AmazonSQSAsyncClient(new BasicAWSCredentials(settings.accessKey, settings.secretKey))
    client.setEndpoint(settings.endpoint)
    client
  }
}

final class SqsSource(queueUrl: String, autoAck: Boolean, sqsClient: AmazonSQSAsyncClient) extends GraphStage[SourceShape[SqsMessage]] {

  val out = Outlet[SqsMessage]("SqsSource.out")

  override val shape: SourceShape[SqsMessage] = SourceShape.of(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) { stage =>

    private val queue = mutable.Queue[SqsMessage]()

    private val consumerCallback = getAsyncCallback(handleDelivery)
    private val failureCallback  = getAsyncCallback(handleFailure)

    def receiveMessagesAsync(): Unit = {
      val request = new ReceiveMessageRequest(queueUrl)
      request.setWaitTimeSeconds(20)
      request.setMaxNumberOfMessages(10)

      sqsClient.receiveMessageAsync(request, new AsyncHandler[ReceiveMessageRequest, ReceiveMessageResult] {
        override def onError(exception: Exception): Unit = failureCallback.invoke(exception)
        override def onSuccess(request: ReceiveMessageRequest, result: ReceiveMessageResult): Unit =
          consumerCallback.invoke(result.getMessages.toList)
      })
    }

    def handleFailure(ex: Exception): Unit = failStage(ex)

    def handleDelivery(messages: List[Message]): Unit = {
      messages.map(m => SqsMessage(m.getMessageId, m.getReceiptHandle, m.getMD5OfBody, m.getBody)) match {
        case head :: tail if isAvailable(out) =>
          queue ++= tail
          pushAndAckMessage(head)
          if (queue.isEmpty) receiveMessagesAsync()
        case all =>
          queue ++= all
      }
    }

    def pushAndAckMessage(message: SqsMessage): Unit = {
      push(out, message)
      sqsClient.deleteMessageAsync(queueUrl, message.receiptHandle)
    }

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {

        if (queue.isEmpty) {
          receiveMessagesAsync()
        } else if (autoAck) {
          pushAndAckMessage(queue.dequeue())
        } else {
          push(out, queue.dequeue())
        }

      }
    })
  }
}
