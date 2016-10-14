package repositories.impl

import models.ShoppingListItem
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.ShoppingListItemRepository
import repositories.impl.tables.ShoppingListItemsTable
import slick.driver.JdbcProfile

import scala.concurrent.Future

private[repositories] class SlickShoppingListItemRepository(protected val dbConfigProvider: DatabaseConfigProvider)
    extends HasDatabaseConfigProvider[JdbcProfile] with ShoppingListItemRepository with ShoppingListItemsTable {

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
