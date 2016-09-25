package controllers

import java.util.UUID
import javax.inject.Inject

import aggregates.TimeEntryAggregate.{ApproveTimeEntry, CreateTimeEntry, DeclineTimeEntry}
import akka.util.Timeout
import cats.data.EitherT
import cats.instances.future._
import model.Fail.{InvalidCommand, InvalidJson, TimeEntryAlreadyExists, TimeEntryDoesNotExist}
import model._
import offices.TimeEntryOffice
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json, Reads}
import play.api.mvc.{Controller, _}
import views.TimeEntryQueryDatabase

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object TimeEntryController {
  case class CreateTimeEntryRequest(begin: DateTime, end: DateTime, timeEntryUserId: UUID)
  implicit val createTimeEntryRequestReads = Json.reads[CreateTimeEntryRequest]
}

class TimeEntryController @Inject()(timeEntryOffice: TimeEntryOffice, timeEntryQueryDatabase: TimeEntryQueryDatabase)(
    implicit ec: ExecutionContext)
    extends Controller
    with Secured
    with JsonFormat {

  import TimeEntryController._

  implicit val timeout = Timeout(5.seconds)

  def create = Authenticated.async(BodyParsers.parse.json) { implicit request =>
    val res = for {
      req <- validate[CreateTimeEntryRequest]
      res <- timeEntryOffice.execute(CreateTimeEntry(UUID.randomUUID(), req.begin, req.end, req.timeEntryUserId, request.userId))
    } yield res

    res.fold(handle, _ => Ok(""))
  }

  def validate[A](implicit request: Request[JsValue], reads: Reads[A], ec: ExecutionContext): Res[A] = {
    request.body
      .validate[A]
      .fold(
        errors => EitherT.left[Future, Fail, A](Future.successful(InvalidJson)),
        a => EitherT.right[Future, Fail, A](Future.successful(a))
      )
  }

  def approve(id: UUID) = Authenticated.async { request =>
    timeEntryOffice.execute(ApproveTimeEntry(id, request.userId)).fold(handle, _ => Ok(""))
  }

  def handle[A](failure: Fail): Result = failure match {
    case InvalidCommand             => BadRequest("invalid command")
    case InvalidJson                => BadRequest("invalid json")
    case TimeEntryAlreadyExists(id) => BadRequest(s"time entry $id already exists")
    case TimeEntryDoesNotExist(id)  => NotFound(s"time entry $id not found")
  }

  def decline(id: UUID) = Authenticated.async { request =>
    timeEntryOffice.execute(DeclineTimeEntry(id, request.userId)).fold(handle, _ => Ok(""))
  }

  def list(userId: UUID) = Authenticated {
    val result = timeEntryQueryDatabase.list(userId)
    Ok(Json.toJson(result))
  }

  def validate[A](reads: Reads[A])(implicit request: Request[JsValue], ec: ExecutionContext): Res[A] = validate(request, reads, ec)

}
