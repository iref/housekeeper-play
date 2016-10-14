package repositories

import com.softwaremill.macwire._
import play.api.db.slick.{DbName, SlickComponents}
import repositories.impl.{SlickShoppingListItemRepository, SlickShoppingListRepository, SlickUserRepository}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * Trait provides repositories implementations.
 */
trait Repositories extends SlickComponents {

  private lazy val dbConfig: DatabaseConfig[JdbcProfile] = api.dbConfig(DbName("default"))

  lazy val shoppingListRepository: ShoppingListRepository = wire[SlickShoppingListRepository]

  lazy val shoppingListItemRepository: ShoppingListItemRepository = wire[SlickShoppingListItemRepository]

  lazy val userRepository: UserRepository = wire[SlickUserRepository]

}
