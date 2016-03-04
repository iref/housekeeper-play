package controllers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.i18n.MessagesApi
import repositories.{ShoppingListItemRepository, ShoppingListRepository, UserRepository}
import services.UserService

/**
 * Bootstrapping of controllers.
 */
trait Controllers {

  def env: Environment[User, CookieAuthenticator]

  def userRepository: UserRepository

  def shoppingListRepository: ShoppingListRepository

  def shoppingListItemRepository: ShoppingListItemRepository

  def messagesApi: MessagesApi

  def passwordHasher: PasswordHasher

  def userService: UserService

  lazy val applicationController = new ApplicationController

  lazy val shoppingListController = new ShoppingListController(shoppingListRepository, messagesApi)

  lazy val shoppingListItemController = new ShoppingListItemController(shoppingListRepository, shoppingListItemRepository, messagesApi)

  lazy val userController = new UserController(userService, passwordHasher, messagesApi)(env)

  lazy val sessionController = new SessionController(userRepository, messagesApi)



}
