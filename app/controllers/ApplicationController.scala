package controllers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.i18n.MessagesApi

class ApplicationController(messagesApi: MessagesApi, env: Environment[User, CookieAuthenticator])
  extends AuthenticatedController(messagesApi, env) {

  def index = SecuredAction { implicit request =>
    Ok(views.html.index(request.identity))
  }
}
