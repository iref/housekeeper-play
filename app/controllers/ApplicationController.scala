package controllers

import play.api.i18n.MessagesApi
import play.api.mvc.Action

class ApplicationController(messagesApi: MessagesApi, webJarAssets: WebJarAssets) extends ViewController(messagesApi, webJarAssets) {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }
}
