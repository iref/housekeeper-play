package controllers

import scala.concurrent.Future

import cats.instances.future._
import com.google.common.base.Charsets
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import models.User
import repositories.UserRepository
import utils.http._

class UserController(
    userRepository: UserRepository,
    messagesApi: MessagesApi) extends ViewController(messagesApi) {

  import UserController._

  def register() = Action { implicit request =>
    Ok(views.html.user.newUser(registrationForm))
  }

  def save() = Action.async { implicit rs =>
    registrationForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.user.newUser(formWithErrors))),
      data => {
        val result = for {
          newUser <- HttpResult.fromFuture(userRepository.save(data.toUser))
          id <- HttpResult(newUser.id)
        } yield Redirect(routes.UserController.show(id))
          .flashing("info" -> "Welcome to Housekeeper! Your account has been created.")
          .withSession("session.username" -> id.toString)

        result.runResult(BadRequest(views.html.user.newUser(registrationForm.fill(data)))
          .flashing("error" -> "Something went wrong"))
      }
    )
  }

  def show(id: Int) = Action.async { implicit rs =>
    val result = for {
      user <- HttpResult(userRepository.find(id))
    } yield Ok(views.html.user.show(UserProfile(user)))
    result.runResult(Redirect(routes.ApplicationController.index()).flashing("error" -> "User profile does not exist."))
  }

  def edit(id: Int) = Action.async { implicit rs =>
    val result = for {
      user <- HttpResult(userRepository.find(id))
    } yield {
      val editUserData = Registration(user.name, user.email, "", "")
      Ok(views.html.user.edit(id, registrationForm.fill(editUserData)))
    }

    result.runResult(Redirect(routes.ApplicationController.index()).flashing("error" -> "User does not exist"))
  }

  def update(id: Int) = Action.async { implicit rs =>
    registrationForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.user.edit(id, formWithErrors)))
      },
      data => {
        val updatedUser = data.toUser.copy(id = Option(id))
        userRepository.update(updatedUser).map(_ => Redirect(routes.UserController.show(id)))
      }
    )
  }
}

object UserController {
  case class Registration(name: String, email: String, password: String, passwordConfirmation: String) {
    def toUser(): User = {
      val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
      User(name, email, hashedPassword)
    }
  }

  val registrationForm = Form(
    mapping(
      "name" -> nonEmptyText(1, 30),
      "email" -> email,
      "password" -> nonEmptyText(6),
      "passwordConfirmation" -> nonEmptyText(6)
    )(Registration.apply)(Registration.unapply) verifying ("Password does not match with confirmation", { formData =>
        formData.password == formData.passwordConfirmation
      })
  )

  case class UserProfile(name: String, email: String) {
    def gravatar(): String = {
      val emailHash = Codecs.md5(email.getBytes(Charsets.UTF_8))
      s"https://secure.gravatar.com/avatar/$emailHash?s=200"
    }
  }

  object UserProfile {
    def apply(user: User): UserProfile = UserProfile(user.name, user.email)
  }
}
