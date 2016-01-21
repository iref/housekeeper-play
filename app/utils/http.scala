package utils

import cats.data.OptionT
import cats.std.future._
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

object http {

  type HttpResult[A] = OptionT[Future, A]

  object HttpResult {
    def apply[A](v: Future[Option[A]]): HttpResult[A] = OptionT(v)
    def apply[A](v: Option[A]): HttpResult[A] = OptionT(Future.successful(v))
    def fromFuture[A](f: Future[A])(implicit ec: ExecutionContext): HttpResult[A] = OptionT(f.map(Option(_)))
  }

  implicit class RichHttpResult(r: HttpResult[Result]) {
    def runResult(orElse: => Result)(implicit ec: ExecutionContext): Future[Result] = r.getOrElse(orElse)
  }

}
