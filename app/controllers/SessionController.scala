package controllers

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LogoutEvent, LoginEvent, Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import services.UserService

import scala.concurrent.Future

class SessionController(
    messagesApi: MessagesApi,
    env: Environment[User, CookieAuthenticator],
    userService: UserService,
    credentialsProvider: CredentialsProvider)
  extends AuthenticatedController(messagesApi, env) {

  import SessionController._

  def login() = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.ApplicationController.index)
      case None => Ok(views.html.session.login(loginForm))
    }
  }

  def logout() = SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index)
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, result)
  }

  def authenticate() = Action.async { implicit rs =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.session.login(formWithErrors))),
      login => {
        val credentials = Credentials(login.email, login.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              for {
                authenticator <- env.authenticatorService.create(loginInfo)
                value <- env.authenticatorService.init(authenticator)
                result <- env.authenticatorService.embed(value, Redirect(routes.UserController.show(user.id.get)).withSession("session.username" -> user.id.get.toString))
              } yield {
                env.eventBus.publish(LoginEvent(user, rs, request2Messages))
                result
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user."))
          }
        } recover {
          case e: ProviderException =>
            Redirect(routes.SessionController.login()).flashing("error" -> Messages("invalid.credentials"))
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
