package offices

import javax.inject.Named

import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import model._

import scala.concurrent.ExecutionContext

@Singleton
class TimeEntryOffice @Inject()(@Named("time-entry-office") officeActor: ActorRef)(implicit ec: ExecutionContext) {

  def execute(command: Command)(implicit timeout: Timeout): Res[Seq[Event]] = {
    EitherT(officeActor.ask(command).mapTo[CmdResult])
  }

}
