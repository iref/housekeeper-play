package controllers

import scala.concurrent.Future

import cats.instances.future._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import models.ShoppingList
import repositories.ShoppingListRepository
import utils.http._

case class ShoppingListData(title: String, description: Option[String])

class ShoppingListController(shoppingListRepository: ShoppingListRepository, messagesApi: MessagesApi)
    extends ViewController(messagesApi) {

  import ShoppingListController._

  def index = Action.async { implicit rs =>
    shoppingListRepository.all.map(shoppingLists => Ok(views.html.shoppingList.index(shoppingLists)))
  }

  def show(id: Int) = Action.async { implicit rs =>
    shoppingListRepository.find(id).map { shoppingListOption =>
      Ok(views.html.shoppingList.show(shoppingListOption, ShoppingListItemController.form))
    }
  }

  def newList() = Action { implicit request =>
    Ok(views.html.shoppingList.newList(form))
  }

  def save() = Action.async { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.shoppingList.newList(formWithErrors))),
      shoppingListData => {
        val shoppingList = ShoppingList(shoppingListData.title, shoppingListData.description)
        val result = for {
          list <- HttpResult.fromFuture(shoppingListRepository.save(shoppingList))
          id <- HttpResult(list.id)
        } yield Redirect(routes.ShoppingListController.show(id))
        result.runResult(Redirect(routes.ShoppingListController.index()).flashing("error" -> "Error while saving newList shopping list"))
      }
    )
  }

  def delete(id: Int) = Action.async { implicit rs =>
    shoppingListRepository.remove(id).map { _ =>
      Redirect(routes.ShoppingListController.index()).flashing("info" -> "Shopping list was removed.")
    }
  }

  def edit(id: Int) = Action.async { implicit rs =>
    val result = HttpResult(shoppingListRepository.find(id)).map { detail =>
      val formData = FormData(detail.shoppingList.title, detail.shoppingList.description)
      Ok(views.html.shoppingList.edit(id, form.fill(formData)))
    }
    result.runResult(Redirect(routes.ShoppingListController.index()).flashing("error" -> "Shopping list does not exist."))
  }

  def update(id: Int) = Action.async { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.shoppingList.edit(1, formWithErrors))),
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
