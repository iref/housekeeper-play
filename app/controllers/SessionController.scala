package controllers

import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, Controller}
import repositories.UserRepository

import scala.concurrent.Future

class SessionController(userRepository: UserRepository, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  import SessionController._

  def login() = Action { implicit request =>
    Ok(views.html.session.login(loginForm))
  }

  def logout() = Action { implicit request =>
    Redirect(routes.ApplicationController.index).withNewSession
  }

  def authenticate() = Action.async { implicit rs =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.session.login(formWithErrors))),
      login => {
        val userFuture = userRepository.findByEmail(login.email)
        userFuture.map { userOption =>
          userOption.filter(u => BCrypt.checkpw(login.password, u.password))
            .map(u => Redirect(routes.UserController.show(u.id.get)).withSession("session.username" -> u.id.get.toString))
            .getOrElse(BadRequest(views.html.session.login(loginForm.withGlobalError("Invalid email address or password."))))
        }
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
