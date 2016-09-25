/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package emailservice.sqs

sealed trait SqsConnectorSettings {
  def endpoint: String

  def queueUrl: String

  def accessKey: String

  def secretKey: String
}

final case class SqsSourceSettings(
    endpoint: String,
    queueUrl: String,
    accessKey: String,
    secretKey: String,
    autoAck: Boolean
) extends SqsConnectorSettings
