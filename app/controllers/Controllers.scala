package controllers

import com.softwaremill.macwire.wire
import play.api.i18n.I18nComponents

import repositories.{ShoppingListItemRepository, ShoppingListRepository, UserRepository}

/**
 * Bootstrapping of controllers.
 */
trait Controllers extends I18nComponents {

  def userRepository: UserRepository

  def shoppingListRepository: ShoppingListRepository

  def shoppingListItemRepository: ShoppingListItemRepository

  lazy val applicationController = wire[ApplicationController]

  lazy val shoppingListController = wire[ShoppingListController]

  lazy val shoppingListItemController = wire[ShoppingListItemController]

  lazy val userController = wire[UserController]

  lazy val sessionController = wire[SessionController]

}
