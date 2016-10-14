package repositories

import play.api.db.slick.DatabaseConfigProvider
import repositories.impl.{SlickShoppingListItemRepository, SlickShoppingListRepository, SlickUserRepository}

/**
 * Trait provides repositories implementations.
 */
trait Repositories {

  def dbConfigProvider: DatabaseConfigProvider

  lazy val shoppingListRepository: ShoppingListRepository = new SlickShoppingListRepository(dbConfigProvider)

  lazy val shoppingListItemRepository: ShoppingListItemRepository = new SlickShoppingListItemRepository(dbConfigProvider)

  lazy val userRepository: UserRepository = new SlickUserRepository(dbConfigProvider)

}
