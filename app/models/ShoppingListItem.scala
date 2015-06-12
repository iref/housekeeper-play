package models

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

import scala.concurrent.Future

case class ShoppingListItem(name: String, quantity: Int, priceForOne: Option[BigDecimal] = None,
                            shoppingListId: Option[Int] = None, id: Option[Int] = None)

trait ShoppingListItemsTable extends ShoppingListTable {
  protected val driver: JdbcProfile

  import driver.api._

  class ShoppingListItems(tag: Tag) extends Table[ShoppingListItem](tag, "shopping_list_items") {
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", ColumnOption.NotNull)
    def quantity = column[Int]("quantity", ColumnOption.NotNull)
    def pricePerItem = column[Option[BigDecimal]]("price_for_one", ColumnOption.Nullable)
    def shoppingListId = column[Option[Int]]("shopping_list_id", ColumnOption.NotNull)

    def * = (name, quantity, pricePerItem, shoppingListId, id) <> ((ShoppingListItem.apply _).tupled, ShoppingListItem.unapply)

    def shoppingList = foreignKey("SHOPPING_LIST_FK", shoppingListId, TableQuery[ShoppingLists])(_.id)
  }
}