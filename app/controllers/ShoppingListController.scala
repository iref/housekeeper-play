package controllers

import models.ShoppingListRepository
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.mvc.Controller

class ShoppingListController(shoppingListRepository: ShoppingListRepository) extends Controller {

  def index = DBAction { implicit rs =>
    val shoppingLists = shoppingListRepository.all
    Ok(views.html.shoppingList.index(shoppingLists))
  }
}
