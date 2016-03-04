package repositories

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import play.api.db.slick.DatabaseConfigProvider
import repositories.impl.{SlickPasswordInfoRepository, SlickShoppingListItemRepository, SlickShoppingListRepository, SlickUserRepository}

/**
 * Trait provides repositories implementations.
 */
trait Repositories {

  def dbConfigProvider: DatabaseConfigProvider

  lazy val shoppingListRepository: ShoppingListRepository = new SlickShoppingListRepository(dbConfigProvider)
  
  lazy val shoppingListItemRepository: ShoppingListItemRepository = new SlickShoppingListItemRepository(dbConfigProvider)

  lazy val userRepository: UserRepository = new SlickUserRepository(dbConfigProvider)

  lazy val passwordInfoRepository: DelegableAuthInfoDAO[PasswordInfo] = new SlickPasswordInfoRepository(dbConfigProvider)

}
