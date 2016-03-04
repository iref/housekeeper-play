package controllers

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import play.api.i18n.MessagesApi
import repositories.{ShoppingListItemRepository, ShoppingListRepository}
import services.UserService

/**
 * Bootstrapping of controllers.
 */
trait Controllers {

  def env: Environment[User, CookieAuthenticator]

  def shoppingListRepository: ShoppingListRepository

  def shoppingListItemRepository: ShoppingListItemRepository

  def messagesApi: MessagesApi

  def passwordHasher: PasswordHasher

  def userService: UserService

  def credentialsProvider: CredentialsProvider

  lazy val applicationController = new ApplicationController(messagesApi, env)

  lazy val shoppingListController = new ShoppingListController(messagesApi, env, shoppingListRepository)

  lazy val shoppingListItemController = new ShoppingListItemController(shoppingListRepository, shoppingListItemRepository, messagesApi, env)

  lazy val userController = new UserController(messagesApi, env, userService, passwordHasher)

  lazy val sessionController = new SessionController(messagesApi, env, userService, credentialsProvider)



}
