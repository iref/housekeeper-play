package controllers

import models.{ShoppingList, ShoppingListRepository}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick._
import play.api.mvc.{Action, Controller}

case class ShoppingListData(title: String, description: Option[String])

class ShoppingListController(shoppingListRepository: ShoppingListRepository) extends Controller {

  import ShoppingListController._

  def index = DBAction { implicit rs =>
    val shoppingLists = shoppingListRepository.all
    Ok(views.html.shoppingList.index(shoppingLists))
  }

  def show(id: Int) = DBAction { implicit rs =>
    val shoppingListDetail = shoppingListRepository.find(id)
    Ok(views.html.shoppingList.show(shoppingListDetail, ShoppingListItemController.form))
  }

  def newList() = Action {
    Ok(views.html.shoppingList.newList(form))
  }

  def save() = DBAction { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.shoppingList.newList(formWithErrors)),
      shoppingListData => {
        val shoppingList = ShoppingList(shoppingListData.title, shoppingListData.description)
        val savedShoppingList = shoppingListRepository.save(shoppingList)
        savedShoppingList.id.map { idVal =>
          Redirect(routes.ShoppingListController.show(idVal))
        } getOrElse {
          Redirect(routes.ShoppingListController.index()).flashing("error" -> "Error while saving newList shopping list")
        }
      }
    )
  }

  def delete(id: Int) = DBAction { implicit rs =>
    shoppingListRepository.remove(id)
    Redirect(routes.ShoppingListController.index()).flashing("info" -> "Shopping list was removed.")
  }

  def edit(id: Int) = DBAction { implicit rs =>
    val detail = shoppingListRepository.find(id)
    detail.map{ d =>
      val formData = FormData(d.shoppingList.title, d.shoppingList.description)
      Ok(views.html.shoppingList.edit(id, form.fill(formData)))
    } getOrElse {
      Redirect(routes.ShoppingListController.index()).flashing("error" -> "Shopping list does not exist.")
    }
  }

  def update(id: Int) = DBAction { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.shoppingList.edit(1, formWithErrors)),
      formData => {
        val updatedShoppingList = ShoppingList(formData.title, formData.description, Some(id))
        shoppingListRepository.update(updatedShoppingList)
        Redirect(routes.ShoppingListController.show(id)).flashing("info" -> "Shopping list was updated.")
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
