package controllers

import models.{ShoppingListItemRepositoryImpl, ShoppingListRepositoryImpl, UserRepositoryImpl}
import play.api.i18n.MessagesApi

/**
 * Bootstrapping of controllers.
 */
trait Controllers {

  lazy val applicationController = new ApplicationController

  lazy val shoppingListController = new ShoppingListController(shoppingListRepository, messagesApi)

  lazy val shoppingListItemController = new ShoppingListItemController(shoppingListRepository, shoppingListItemRepository, messagesApi)

  lazy val userController = new UserController(userRepository, messagesApi)

  lazy val sessionController = new SessionController(userRepository, messagesApi)

  def userRepository: UserRepositoryImpl

  def shoppingListRepository: ShoppingListRepositoryImpl

  def shoppingListItemRepository: ShoppingListItemRepositoryImpl

  def messagesApi: MessagesApi

}
