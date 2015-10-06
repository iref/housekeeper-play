package repositories.impl.tables

import models.ShoppingList
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

private[impl] trait ShoppingListTable {
  protected val driver: JdbcProfile

  import driver.api._

  class ShoppingLists(tag: Tag) extends Table[ShoppingList](tag, "shopping_lists") {
    def title = column[String]("title", ColumnOption.NotNull)
    def description = column[Option[String]]("description", ColumnOption.Nullable)
    def id = column[Option[Int]]("id", slick.ast.ColumnOption.PrimaryKey, slick.ast.ColumnOption.AutoInc)

    def * = (title, description, id) <> ((ShoppingList.apply _).tupled, ShoppingList.unapply)
  }

  protected val shoppingLists = TableQuery[ShoppingLists]
}
