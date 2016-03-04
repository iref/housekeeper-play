package controllers

import com.mohiva.play.silhouette.api.{Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.i18n.MessagesApi
import play.api.mvc.{Result, RequestHeader}

import scala.concurrent.Future

abstract class AuthenticatedController(
    val messagesApi: MessagesApi,
    val env: Environment[User, CookieAuthenticator])
  extends Silhouette[User, CookieAuthenticator] {

  override protected def onNotAuthenticated(request: RequestHeader): Option[Future[Result]] = {
    Option(Future.successful(Redirect(routes.SessionController.login())))
  }
}
