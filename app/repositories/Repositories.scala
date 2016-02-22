package repositories

import play.api.db.slick.DatabaseConfigProvider
import repositories.impl.{SlickHouseholdRepository, SlickShoppingListItemRepository, SlickShoppingListRepository, SlickUserRepository}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * Trait provides repositories implementations.
 */
trait Repositories {

  def dbConfig: DatabaseConfig[JdbcProfile]

  lazy val shoppingListRepository: ShoppingListRepository = new SlickShoppingListRepository(dbConfig)
  
  lazy val shoppingListItemRepository: ShoppingListItemRepository = new SlickShoppingListItemRepository(dbConfig)

  lazy val userRepository: UserRepository = new SlickUserRepository(dbConfig)

  lazy val householdRepository: HouseholdRepository = new SlickHouseholdRepository(dbConfig)

}
