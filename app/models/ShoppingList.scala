package models

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class ShoppingList(title: String, description: Option[String] = None, id: Option[Int] = None)

case class ShoppingListDetail(shoppingList: ShoppingList, items: Seq[ShoppingListItem])

trait ShoppingListTable {
  protected val driver: JdbcProfile

  import driver.api._

  class ShoppingLists(tag: Tag) extends Table[ShoppingList](tag, "shopping_lists") {
    def title = column[String]("title", ColumnOption.NotNull)
    def description = column[Option[String]]("description", ColumnOption.Nullable)
    def id = column[Option[Int]]("id", slick.ast.ColumnOption.PrimaryKey, slick.ast.ColumnOption.AutoInc)

    def * = (title, description, id) <> ((ShoppingList.apply _).tupled, ShoppingList.unapply)
  }

}
