package models

import play.api.db.slick.DatabaseConfigProvider

/**
 * Trait provides repositories implementations.
 */
trait Repositories {

  def dbConfigProvider: DatabaseConfigProvider

  lazy val shoppingListRepository = new ShoppingListRepositoryImpl(dbConfigProvider)
  
  lazy val shoppingListItemRepository = new ShoppingListItemRepositoryImpl(dbConfigProvider)

  lazy val userRepository = new UserRepositoryImpl(dbConfigProvider)

}
