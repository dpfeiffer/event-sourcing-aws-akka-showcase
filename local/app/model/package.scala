import cats.data.EitherT

import scala.concurrent.Future
package object model {
  type CmdResult = Either[Fail, Seq[Event]]
  type Res[A] = EitherT[Future, Fail, A]
}
