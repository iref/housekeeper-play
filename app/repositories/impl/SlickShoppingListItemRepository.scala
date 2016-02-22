package repositories.impl

import scala.concurrent.Future

import models.ShoppingListItem
import play.api.db.slick.HasDatabaseConfig
import repositories.ShoppingListItemRepository
import repositories.impl.tables.ShoppingListItemsTable
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

private[repositories] class SlickShoppingListItemRepository(protected val dbConfig: DatabaseConfig[JdbcProfile])
  extends HasDatabaseConfig[JdbcProfile] with ShoppingListItemRepository with ShoppingListItemsTable {

  import driver.api._

  def add(id: Int, item: ShoppingListItem): Future[ShoppingListItem] = {
    val withListId = item.copy(shoppingListId = Some(id))
    val insertWithId = (shoppingListItems returning shoppingListItems.map(_.id))
      .into((_, id) => withListId.copy(id = id))
    val q = insertWithId += withListId
    db.run(q.transactionally)
  }

  def remove(id: Int): Future[Int] = {
    val query = shoppingListItems.filter(_.id === id).delete
    db.run(query.transactionally)
  }

  def update(item: ShoppingListItem): Future[Int] = {
    val query = shoppingListItems.filter(_.id === item.id).update(item)
    db.run(query.transactionally)
  }

  def find(id: Int): Future[Option[ShoppingListItem]] = {
    val query = shoppingListItems.filter(_.id === id)
    db.run(query.result.headOption)
  }
}
