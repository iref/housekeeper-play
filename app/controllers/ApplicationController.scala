package controllers

import play.api.i18n.MessagesApi
import play.api.mvc.Action

class ApplicationController(messagesApi: MessagesApi) extends ViewController(messagesApi) {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }
}
