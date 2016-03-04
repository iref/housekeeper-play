package controllers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.{ShoppingList, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import repositories.ShoppingListRepository

import scala.concurrent.Future

case class ShoppingListData(title: String, description: Option[String])

class ShoppingListController(messagesApi: MessagesApi,
    env: Environment[User, CookieAuthenticator],
    shoppingListRepository: ShoppingListRepository)
  extends AuthenticatedController(messagesApi, env) {

  import ShoppingListController._

  def index = SecuredAction.async { implicit rs =>
    shoppingListRepository.all.map(shoppingLists => Ok(views.html.shoppingList.index(shoppingLists, rs.identity)))
  }

  def show(id: Int) = SecuredAction.async { implicit rs =>
    shoppingListRepository.find(id).map { shoppingListOption =>
      Ok(views.html.shoppingList.show(shoppingListOption, ShoppingListItemController.form, rs.identity))
    }
  }

  def newList() = SecuredAction { implicit request =>
    Ok(views.html.shoppingList.newList(form, request.identity))
  }

  def save() = SecuredAction.async { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.shoppingList.newList(formWithErrors, rs.identity))),
      shoppingListData => {
        val shoppingList = ShoppingList(shoppingListData.title, shoppingListData.description)
        shoppingListRepository.save(shoppingList).map { saved =>
          saved.id.map { idVal =>
            Redirect(routes.ShoppingListController.show(idVal))
          } getOrElse {
            Redirect(routes.ShoppingListController.index()).flashing("error" -> "Error while saving newList shopping list")
          }
        }
      }
    )
  }

  def delete(id: Int) = SecuredAction.async { implicit rs =>
    shoppingListRepository.remove(id).map { _ =>
      Redirect(routes.ShoppingListController.index()).flashing("info" -> "Shopping list was removed.")
    }
  }

  def edit(id: Int) = SecuredAction.async { implicit rs =>
    shoppingListRepository.find(id).map { detailOption =>
      detailOption.map{ detail =>
        val formData = FormData(detail.shoppingList.title, detail.shoppingList.description)
        Ok(views.html.shoppingList.edit(id, form.fill(formData), rs.identity))
      } getOrElse {
        Redirect(routes.ShoppingListController.index()).flashing("error" -> "Shopping list does not exist.")
      }
    }
  }

  def update(id: Int) = SecuredAction.async { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.shoppingList.edit(1, formWithErrors, rs.identity))),
      formData => {
        val updatedShoppingList = ShoppingList(formData.title, formData.description, Some(id))
        shoppingListRepository.update(updatedShoppingList).map { _ =>
          Redirect(routes.ShoppingListController.show(id)).flashing("info" -> "Shopping list was updated.")
        }
      }
    )
  }
}

object ShoppingListController {
  case class FormData(title: String, description: Option[String])

  private val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "description" -> optional(text)
    )(FormData.apply)(FormData.unapply))
}
