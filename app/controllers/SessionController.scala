package controllers

import models.UserRepository
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import play.api.db.slick._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, Controller}

class SessionController(userRepository: UserRepository) extends Controller with I18nSupportsb {
  import SessionController._

  def login() = Action { implicit request =>
    Ok(views.html.session.login(loginForm))
  }

  def logout() = Action { implicit request =>
    Redirect(routes.Application.index).withNewSession
  }

  def authenticate() = DBAction { implicit rs =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.session.login(formWithErrors)),
      login => {
        userRepository.findByEmail(login.email)
          .filter(u => BCrypt.checkpw(login.password, u.password))
          .map(u => Redirect(routes.UserController.show(u.id.get)).withSession("session.username" -> u.id.get.toString))
          .getOrElse(BadRequest(views.html.session.login(loginForm.withGlobalError("Invalid email address or password."))))
      }
    )
  }
}

object SessionController {
  case class Login(email: String, password: String)

  val loginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText()
    )(Login.apply)(Login.unapply)
  )
}
