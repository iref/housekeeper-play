package controllers

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

class ShoppingListItemController(shoppingListRepository: ShoppingListRepositoryImpl,
                                 shoppingListItemRepository: ShoppingListItemRepositoryImpl,
                                 val messagesApi: MessagesApi) extends Controller with I18nSupport {

  import ShoppingListItemController._

  def save(listId: Int) = Action.async { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => {
        shoppingListRepository.find(listId).map { detailOption =>
          BadRequest(views.html.shoppingList.show(detailOption, formWithErrors))
        }
      },
      listItem => {
        val item = ShoppingListItem(listItem.name, listItem.quantity, listItem.priceForOne, Some(listId))
        shoppingListItemRepository.add(listId, item).map { sli =>
          sli.id.map { idVal =>
            Redirect(routes.ShoppingListController.show(listId))
          } getOrElse {
            Redirect(routes.ShoppingListController.show(listId)).flashing(("error" -> "Error while saving newList shopping item"))
          }
        }
      }
    )
  }

  def remove(id: Int, listId: Int) = Action.async { implicit rs =>
    shoppingListItemRepository.remove(id).map{ _ =>
      Redirect(routes.ShoppingListController.show(listId)).flashing(("info" -> "Item was deleted."))
    }
  }

  def edit(id: Int, listId: Int) = Action.async { implicit rs =>
    shoppingListItemRepository.find(id).map { itemOption =>
      itemOption.map { item =>
        val formData = FormData(item.name, item.quantity, item.priceForOne)
        Ok(views.html.shoppingListItem.edit(id, listId, form.fill(formData)))
      } getOrElse {
        Redirect(routes.ShoppingListController.show(listId)).flashing("error" -> "Item does not exists.")
      }
    }
  }

  def update(id: Int, listId: Int) = Action.async { implicit rs =>
    ShoppingListItemController.form.bindFromRequest.fold(
      formWithError => Future(BadRequest(views.html.shoppingListItem.edit(id, listId, formWithError))),
      listItem => {
        val toUpdate = ShoppingListItem(listItem.name, listItem.quantity, listItem.priceForOne, Some(listId), Some(id))
        shoppingListItemRepository.update(toUpdate).map { _ =>
          Redirect(routes.ShoppingListController.show(listId)).flashing("info" -> "Item was updated.")
        }
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
