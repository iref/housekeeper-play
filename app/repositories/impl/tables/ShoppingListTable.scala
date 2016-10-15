package repositories.impl.tables

import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

import models.ShoppingList

private[impl] trait ShoppingListTable {
  protected val driver: JdbcProfile

  import driver.api._

  class ShoppingLists(tag: Tag) extends Table[ShoppingList](tag, "shopping_lists") {
    def title = column[String]("title", ColumnOption.NotNull)
    def description = column[Option[String]]("description", ColumnOption.Nullable)
    def id = column[Option[Int]]("id", slick.ast.ColumnOption.PrimaryKey, slick.ast.ColumnOption.AutoInc)

    // scalastyle:off
    def * = (title, description, id) <> ((ShoppingList.apply _).tupled, ShoppingList.unapply)
    // scalastyle:on
  }

  protected val shoppingLists = TableQuery[ShoppingLists]
}
