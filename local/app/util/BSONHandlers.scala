package util

import java.util.UUID

import org.joda.time.DateTime
import reactivemongo.bson.{BSONDateTime, BSONHandler, BSONString}

object BSONHandlers {
  implicit val uuidHandler =  new BSONHandler[BSONString,UUID]{
    override def write(t: UUID): BSONString = BSONString(t.toString)
    override def read(bson: BSONString): UUID = UUID.fromString(bson.value)
  }
  implicit val dateTimeHandler = new BSONHandler[BSONDateTime, DateTime] {
    override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)

    override def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }
}
