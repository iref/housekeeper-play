package controllers

import models.{ShoppingListItemRepository, ShoppingListItem, ShoppingListRepository}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.i18n.I18nSupport
import play.api.mvc.Controller

class ShoppingListItemController(shoppingListRepository: ShoppingListRepository,
                                 shoppingListItemRepository: ShoppingListItemRepository) extends Controller with I18nSupport {

  import ShoppingListItemController._

  def save(listId: Int) = DBAction { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => {
        val shoppingList = shoppingListRepository.find(listId)
        BadRequest(views.html.shoppingList.show(shoppingList, formWithErrors))
      },
      listItem => {
        val item = ShoppingListItem(listItem.name, listItem.quantity, listItem.priceForOne, Some(listId))
        val savedItem = shoppingListItemRepository.add(listId, item)
        savedItem.id.map { idVal =>
          Redirect(routes.ShoppingListController.show(listId))
        } getOrElse {
          Redirect(routes.ShoppingListController.show(listId)).flashing(("error" -> "Error while saving newList shopping item"))
        }
      }
    )
  }

  def remove(id: Int, listId: Int) = DBAction { implicit rs =>
    shoppingListItemRepository.remove(id)
    Redirect(routes.ShoppingListController.show(listId)).flashing(("info" -> "Item was deleted."))
  }

  def edit(id: Int, listId: Int) = DBAction { implicit rs =>
    shoppingListItemRepository.find(id).map { item =>
      val formData = FormData(name = item.name,
        quantity = item.quantity, priceForOne = item.priceForOne)
      Ok(views.html.shoppingListItem.edit(id, listId, form.fill(formData)))
    }.get
  }

  def update(id: Int, listId: Int) = DBAction { implicit rs =>
    ShoppingListItemController.form.bindFromRequest.fold(
      formWithError => BadRequest(views.html.shoppingListItem.edit(id, listId, formWithError)),
      listItem => {
        val toUpdate = ShoppingListItem(listItem.name, listItem.quantity, listItem.priceForOne, Some(listId), Some(id))
        shoppingListItemRepository.update(toUpdate)
        Redirect(routes.ShoppingListController.show(listId)).flashing("info" -> "Item was updated.")
      }
    )
  }
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
