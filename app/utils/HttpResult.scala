package utils

import cats.data.{Xor,XorT}
import cats.std.future._
import play.api.mvc.Result
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object http {

  type HttpResult[A] = XorT[Future, Result, A]

  object HttpResult {

    def apply[A](v: Future[Xor[Result, A]]): HttpResult[A] = HttpResult(v)

    def apply[A](v: Xor[Result, A]): HttpResult[A] = HttpResult(Future.successful(v))

    def fromFuture[A](v: Future[A])(implicit ec: ExecutionContext): HttpResult[A] =
      HttpResult(v.map(Xor.right))

    def fromOption[A](v: Option[A])(forNone: => Result): HttpResult[A] = HttpResult(Xor.fromOption(v, forNone))

    def fromXor[A, B](v: Xor[B, A])(forLeft: B => Result): HttpResult[A] = HttpResult(v.leftMap(forLeft)) 

    def fromFutureOption[A](v: Future[Option[A]])(forNone: => Result)(implicit ec: ExecutionContext): HttpResult[A] =
      HttpResult(v.map(o => Xor.fromOption(o, forNone)))

  }

  implicit class RichHttpResult(httpResult: HttpResult[Result]) {
    def runResult(implicit ec: ExecutionContext): Future[Result] = httpResult.value.map(_.merge)
  }

}
