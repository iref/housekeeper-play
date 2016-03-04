package controllers

import com.google.common.base.Charsets
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent, Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, Controller}
import repositories.UserRepository

import scala.concurrent.Future

import services.UserService

class UserController(
    messagesApi: MessagesApi,
    env: Environment[User, CookieAuthenticator],
    userService: UserService,
    passwordHasher: PasswordHasher)
  extends AuthenticatedController(messagesApi, env) {

  import UserController._

  def register() = Action { implicit request =>
    Ok(views.html.user.newUser(registrationForm))
  }

  def save() = Action.async { implicit rs =>
    registrationForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.user.newUser(formWithErrors))),
      data => {
        val authInfo = passwordHasher.hash(data.password)
        val user = User(data.name, data.email, authInfo.password)

        for {
          user <- userService.save(user)
          authenticator <- env.authenticatorService.create(user.loginInfo)
          value <- env.authenticatorService.init(authenticator)
          result <- env.authenticatorService.embed(value, Redirect(routes.UserController.show(user.id.get)))
        } yield {
          env.eventBus.publish(SignUpEvent(user, rs, request2Messages))
          env.eventBus.publish(LoginEvent(user, rs, request2Messages))
          result
        }
      }
    )
  }

  def show(id: Int) = Action.async { implicit rs =>
    val userFuture = userService.find(id)
    userFuture.map { userOption =>
      userOption.map { user =>
        Ok(views.html.user.show(UserProfile(user)))
      } getOrElse {
        Redirect(routes.ApplicationController.index()).flashing("error" -> "User profile does not exist.")
      }
    }
  }

  def edit(id: Int) = SecuredAction.async { implicit rs =>
    userService.find(id).map { userOption =>
      userOption.map { u =>
        val editUserData = Registration(u.name, u.email, "", "")
        Ok(views.html.user.edit(rs.identity, registrationForm.fill(editUserData)))
      } getOrElse {
        Redirect(routes.ApplicationController.index()).flashing("error" -> "User does not exist")
      }
    }
  }

  def update(id: Int) = SecuredAction.async { implicit rs =>
    registrationForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.user.edit(rs.identity, formWithErrors)))
      },
      data => {
        val updatedUser = User(data.name, data.email, BCrypt.hashpw(data.password, BCrypt.gensalt()), Option(id))
        userService.save(updatedUser).map(_ => Redirect(routes.UserController.show(id)))
      }
    )
  }
}

object UserController {
  case class Registration(name: String, email: String, password: String, passwordConfirmation: String)

  val registrationForm = Form(
    mapping(
      "name" -> nonEmptyText(1, 30),
      "email" -> email,
      "password" -> nonEmptyText(6),
      "passwordConfirmation" -> nonEmptyText(6)
    )(Registration.apply)(Registration.unapply) verifying("Password does not match with confirmation", { formData =>
      formData.password == formData.passwordConfirmation
    })
  )

  case class UserProfile(user: User) {
    def name: String = user.name
    def gravatar(): String = {
      val emailHash = Codecs.md5(user.email.getBytes(Charsets.UTF_8))
      s"https://secure.gravatar.com/avatar/$emailHash?s=200"
    }
  }
}
