package models

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

import scala.concurrent.Future

case class ShoppingListItem(name: String, quantity: Int, priceForOne: Option[BigDecimal] = None,
                            shoppingListId: Option[Int] = None, id: Option[Int] = None)

trait ShoppingListItemRepository { self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  class ShoppingListItemTable(tag: Tag) extends Table[ShoppingListItem](tag, "shopping_list_items") {
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", ColumnOption.NotNull)
    def quantity = column[Int]("quantity", ColumnOption.NotNull)
    def pricePerItem = column[Option[BigDecimal]]("price_for_one", ColumnOption.Nullable)
    def shoppingListId = column[Option[Int]]("shopping_list_id", ColumnOption.NotNull)

    def * = (name, quantity, pricePerItem, shoppingListId, id) <> ((ShoppingListItem.apply _).tupled, ShoppingListItem.unapply)
  }
}

class ShoppingListItemRepositoryImpl(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with ShoppingListItemRepository {

  import driver.api._

  private val shoppingListItems = TableQuery[ShoppingListItemTable]

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