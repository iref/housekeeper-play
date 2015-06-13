package controllers

import com.google.common.base.Charsets
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

class UserController(userRepository: UserRepository,
                     val messagesApi: MessagesApi) extends Controller with I18nSupport {

  import UserController._

  def register() = Action { implicit request =>
    Ok(views.html.user.newUser(registrationForm))
  }

  def save() = Action.async { implicit rs =>
    registrationForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.user.newUser(formWithErrors))),
      data => {
        userRepository.save(data.toUser).map { newUser =>
          Redirect(routes.UserController.show(newUser.id.get))
            .flashing("info" -> "Welcome to Housekeeper! Your account has been created.")
            .withSession("session.username" -> newUser.id.get.toString)
        }
      }
    )
  }

  def show(id: Int) = Action.async { implicit rs =>
    val userFuture = userRepository.find(id)
    userFuture.map { userOption =>
      userOption.map { user =>
        Ok(views.html.user.show(UserProfile(user)))
      } getOrElse {
        Redirect(routes.ApplicationController.index()).flashing("error" -> "User profile does not exist.")
      }
    }
  }

  def edit(id: Int) = Action.async { implicit rs =>
    userRepository.find(id).map { userOption =>
      userOption.map { u =>
        val editUserData = Registration(u.name, u.email, "", "")
        Ok(views.html.user.edit(id, registrationForm.fill(editUserData)))
      } getOrElse {
        Redirect(routes.ApplicationController.index()).flashing("error" -> "User does not exist")
      }
    }
  }

  def update(id: Int) = Action.async { implicit rs =>
    registrationForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.user.edit(id, formWithErrors)))
      },
      data => {
        val updatedUser = User(data.name, data.email, BCrypt.hashpw(data.password, BCrypt.gensalt()), Option(id))
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
    )(Registration.apply)(Registration.unapply) verifying("Password does not match with confirmation", { formData =>
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
