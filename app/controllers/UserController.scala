package controllers

import models.{User, UserRepository}
import org.mindrot.jbcrypt.BCrypt
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick._
import play.api.mvc.{Action, Controller}

class UserController(userRepository: UserRepository) extends Controller {

  import UserController._

  def register() = Action {
    Ok(views.html.user.newUser(form))
  }

  def save() = DBAction { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.user.newUser(formWithErrors)),
      data => {
        val newUser = transform(data)
        val stored = userRepository.save(newUser)
        Redirect(routes.Application.index()).flashing("info" -> "Welcome to Housekeeper! Your account has been created.")
      }
    )
  }

  def show(id: Int) = DBAction { implicit rs =>
    userRepository.find(id).map { u =>
      Ok(views.html.user.show(u))
    } getOrElse {
      Redirect(routes.Application.index()).flashing("error" -> "User profile does not exist.")
    }
  }
}

object UserController {
  case class FormData(name: String, email: String, password: String, passwordConfirmation: String)

  def transform(data: FormData): User = {
    val hashedPassword = BCrypt.hashpw(data.password, BCrypt.gensalt())
    User(data.name, data.email, hashedPassword)
  }

  val form = Form(
    mapping(
      "name" -> nonEmptyText(1, 30),
      "email" -> email,
      "password" -> nonEmptyText(6),
      "passwordConfirmation" -> nonEmptyText(6)
    )(FormData.apply)(FormData.unapply) verifying("Password does not match with confirmation", { formData =>
      formData.password == formData.passwordConfirmation
    })
  )
}
