package controllers

import models.{ShoppingListItem, ShoppingList, ShoppingListRepository}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.mvc.{Action, Controller}

case class ShoppingListData(title: String, description: Option[String])

case class ShoppingListItemData(name: String, quantity: Int, priceForOne: Option[BigDecimal])

class ShoppingListController(shoppingListRepository: ShoppingListRepository) extends Controller {

  private val shoppingListForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "description" -> optional(text)
    )(ShoppingListData.apply)(ShoppingListData.unapply))

  private val listItemForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "quantity" -> number(min = 1),
      "priceForOne" -> optional(bigDecimal(10, 2) verifying(min(BigDecimal(0))))
    )(ShoppingListItemData.apply)(ShoppingListItemData.unapply))

  def index = DBAction { implicit rs =>
    val shoppingLists = shoppingListRepository.all
    Ok(views.html.shoppingList.index(shoppingLists))
  }

  def show(id: Int) = DBAction { implicit rs =>
    val shoppingListDetail = shoppingListRepository.find(id)
    Ok(views.html.shoppingList.show(shoppingListDetail, listItemForm))
  }

  def newList() = Action {
    Ok(views.html.shoppingList.edit(shoppingListForm))
  }

  def save() = DBAction { implicit rs =>
    shoppingListForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.shoppingList.edit(formWithErrors)),
      shoppingListData => {
        val shoppingList = ShoppingList(shoppingListData.title, shoppingListData.description)
        val savedShoppingList = shoppingListRepository.save(shoppingList)
        savedShoppingList.id.map { idVal =>
          Redirect(routes.ShoppingListController.show(idVal))
        } getOrElse {
          Redirect(routes.ShoppingListController.index()).flashing(("error" -> "Error while saving new shopping list"))
        }
      }
    )
  }

  def addItem(id: Int) = DBAction { implicit rs =>
    listItemForm.bindFromRequest.fold(
      formWithErrors => {
        val shoppingList = shoppingListRepository.find(id)
        BadRequest(views.html.shoppingList.show(shoppingList, formWithErrors))
      },
      listItem => {
        val item = ShoppingListItem(listItem.name, listItem.quantity, listItem.priceForOne, Some(id))
        val savedItem = shoppingListRepository.addItem(id, item)
        savedItem.id.map { idVal =>
          Redirect(routes.ShoppingListController.show(id))
        } getOrElse {
          Redirect(routes.ShoppingListController.show(id)).flashing(("error" -> "Error while saving new shopping item"))
        }
      }
    )
  }

  def removeItem(id: Int, listId: Int) = DBAction { implicit rs =>
    shoppingListRepository.removeItem(id)
    Redirect(routes.ShoppingListController.show(listId)).flashing(("info" -> "Item was deleted."))
  }
}
