package controllers

import java.util.UUID

import play.api.mvc.Results._
import play.api.mvc.{ActionBuilder, Request, Result, WrappedRequest}

import scala.concurrent.Future

case class AuthenticatedRequest[A](userId: UUID, request: Request[A]) extends WrappedRequest[A](request)

trait Secured {

  def Authenticated = new AuthenticatedAction

}

class AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    request.headers.get("Authorization") match {
      case None => Future.successful(Unauthorized)
      case Some(x) => block(AuthenticatedRequest(UUID.fromString(x),request))
    }
  }
}
