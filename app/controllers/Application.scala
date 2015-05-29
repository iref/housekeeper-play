package controllers

import play.api.mvc.{Action, Controller}
import play.api.Play.current

class Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }
}
