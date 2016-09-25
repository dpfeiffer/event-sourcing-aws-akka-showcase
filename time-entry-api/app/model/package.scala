import cats.data.EitherT
import showcase.events.Event

import scala.concurrent.Future
package object model {
  type CmdResult = Either[Fail, Seq[Event]]
  type Res[A] = EitherT[Future, Fail, A]
}
