package controllers

import com.softwaremill.macwire.MacwireMacros._
import models.{ShoppingListItemRepositoryImpl, ShoppingListRepositoryImpl, ShoppingListRepository, UserRepositoryImpl}
import play.api.i18n.MessagesApi

/**
 * Bootstrapping of controllers.
 */
trait Controllers {

  lazy val applicationController = new Application

  lazy val shoppingListController = new ShoppingListController(shoppingListRepository, messagesApi)

  lazy val shoppingListItemController = new ShoppingListItemController(shoppingListRepository, shoppingListItemRepository, messagesApi)

  lazy val userController = new UserController(userRepository, messagesApi)

  lazy val sessionController = new SessionController(userRepository, messagesApi)

  def userRepository: UserRepositoryImpl

  def shoppingListRepository: ShoppingListRepositoryImpl

  def shoppingListItemRepository: ShoppingListItemRepositoryImpl

  def messagesApi: MessagesApi

}
