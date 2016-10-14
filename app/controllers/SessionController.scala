package controllers

import cats.std.future._
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, Controller}
import repositories.UserRepository
import utils.http._

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
        val result = HttpResult(userRepository.findByEmail(login.email))
          .filter(user => BCrypt.checkpw(login.password, user.password))
          .flatMap(user => HttpResult(user.id))
          .map { id =>
            Redirect(routes.UserController.show(id))
              .withSession("session.username" -> id.toString)
          }

        result.runResult(
          BadRequest(views.html.session.login(loginForm
            .withGlobalError("Invalid email address or password.")))
        )
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
