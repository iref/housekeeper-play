package controllers

import play.api.mvc.{Action, Controller}
import play.api.Play.current

object Application extends Controller {

  def index = Action { request =>
    Ok(views.html.index())
  }
}
