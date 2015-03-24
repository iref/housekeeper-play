package controllers

import models.{ShoppingListItem, ShoppingListRepository}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.mvc.{Action, Controller}

class ShoppingListItemController(shoppingListRepository: ShoppingListRepository) extends Controller {

  import ShoppingListItemController._

  def save(listId: Int) = DBAction { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => {
        val shoppingList = shoppingListRepository.find(listId)
        BadRequest(views.html.shoppingList.show(shoppingList, formWithErrors))
      },
      listItem => {
        val item = ShoppingListItem(listItem.name, listItem.quantity, listItem.priceForOne, Some(listId))
        val savedItem = shoppingListRepository.addItem(listId, item)
        savedItem.id.map { idVal =>
          Redirect(routes.ShoppingListController.show(listId))
        } getOrElse {
          Redirect(routes.ShoppingListController.show(listId)).flashing(("error" -> "Error while saving new shopping item"))
        }
      }
    )
  }

  def remove(id: Int, listId: Int) = DBAction { implicit rs =>
    shoppingListRepository.removeItem(id)
    Redirect(routes.ShoppingListController.show(listId)).flashing(("info" -> "Item was deleted."))
  }

  def edit(id: Int, listId: Int) = Action { NotImplemented }

  def update(id: Int, listId: Int) = Action { NotImplemented }
}

object ShoppingListItemController {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "quantity" -> number(min = 1),
      "priceForOne" -> optional(bigDecimal(10, 2) verifying(min(BigDecimal(0))))
    )(FormData.apply)(FormData.unapply))

  case class FormData(name: String, quantity: Int, priceForOne: Option[BigDecimal])
}
