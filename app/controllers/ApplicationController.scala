package controllers

import play.api.mvc.{Action, Controller}

class ApplicationController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }
}
