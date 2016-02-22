package controllers

import play.api.i18n.MessagesApi
import repositories.{ShoppingListItemRepository, ShoppingListRepository, UserRepository}

/**
 * Bootstrapping of controllers.
 */
trait Controllers {

  lazy val applicationController = new ApplicationController

  lazy val shoppingListController = new ShoppingListController(shoppingListRepository, messagesApi)

  lazy val shoppingListItemController = new ShoppingListItemController(
      shoppingListRepository,
      shoppingListItemRepository,
      messagesApi)

  lazy val userController = new UserController(userRepository, messagesApi)

  lazy val sessionController = new SessionController(userRepository, messagesApi)

  def userRepository: UserRepository

  def shoppingListRepository: ShoppingListRepository

  def shoppingListItemRepository: ShoppingListItemRepository

  def messagesApi: MessagesApi

}
