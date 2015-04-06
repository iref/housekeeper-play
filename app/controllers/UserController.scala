package controllers

import com.google.common.base.Charsets
import models.{User, UserRepository}
import org.mindrot.jbcrypt.BCrypt
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick._
import play.api.libs.Codecs
import play.api.mvc.{Action, Controller}

class UserController(userRepository: UserRepository) extends Controller {

  import UserController._

  val loginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText()
    )(Login.apply)(Login.unapply)
  )

  def register() = Action {
    Ok(views.html.user.newUser(registrationForm))
  }

  def save() = DBAction { implicit rs =>
    registrationForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.user.newUser(formWithErrors)),
      data => {
        val newUser = userRepository.save(data.toUser)
        Redirect(routes.UserController.show(newUser.id.get)).flashing("info" -> "Welcome to Housekeeper! Your account has been created.")
      }
    )
  }

  def show(id: Int) = DBAction { implicit rs =>
    userRepository.find(id).map { u =>
      Ok(views.html.user.show(UserProfile(u)))
    } getOrElse {
      Redirect(routes.Application.index()).flashing("error" -> "User profile does not exist.")
    }
  }

  def login() = Action {
    Ok(views.html.user.login(loginForm))
  }

  def authenticate() = DBAction { implicit rs =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.user.login(formWithErrors)),
      login => {
        userRepository.findByEmail(login.email)
          .filter(u => BCrypt.checkpw(login.password, u.password))
          .map(u => Redirect(routes.UserController.show(u.id.get)).withSession("session.username" -> u.id.get.toString))
          .getOrElse(BadRequest(views.html.user.login(loginForm.withGlobalError("Invalid email address or password."))))
      }
    )
  }

  def edit(id: Int) = DBAction { implicit rs =>
    userRepository.find(id).map { u =>
      val editUserData = EditUserFormData(u.name, u.email, None, None, None)
      Ok(views.html.user.edit(id, editUserForm.fill(editUserData)))  
    } getOrElse {
      Redirect(routes.Application.index()).flashing("error" -> "User does not exist")
    }
  }

  def update(id: Int) = Action { NotImplemented }
}

object UserController {
  case class Registration(name: String, email: String, password: String, passwordConfirmation: String) {
    def toUser(): User = {
      val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
      User(name, email, hashedPassword)
    }
  }

  case class UserProfile(name: String, email: String) {
    def gravatar(): String = {
      val emailHash = Codecs.md5(email.getBytes(Charsets.UTF_8))
      s"https://secure.gravatar.com/avatar/$emailHash?s=200"
    }
  }

  object UserProfile {
    def apply(user: User): UserProfile = UserProfile(user.name, user.email)
  }

  case class Login(email: String, password: String)

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

  case class EditUserFormData(name: String, email: String, oldPassword: Option[String],
                              password: Option[String], passwordConfirmation: Option[String])

  val editUserForm = Form(
    mapping(
      "name" -> nonEmptyText(1, 30),
      "email" -> email,
      "oldPassword" -> optional(nonEmptyText),
      "password" -> optional(nonEmptyText(6)),
      "passwordConfirmation" -> optional(nonEmptyText(6))
    )(EditUserFormData.apply)(EditUserFormData.unapply)
  )
}
