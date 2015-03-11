package models

import play.api.db.slick.Config.driver.simple._

case class ShoppingList(title: String, description: String, id: Option[Int] = None)

object ShoppingList {
  class ShoppingListsTable(tag: Tag) extends Table[ShoppingList](tag, "shopping_lists") {
    def title = column[String]("title", O.NotNull)
    def description = column[String]("description", O.Nullable)
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)

    def * = (title, description, id) <> ((ShoppingList.apply _).tupled, ShoppingList.unapply)
  }

  val table = TableQuery[ShoppingListsTable]
}

class ShoppingListRepository {

  def all(implicit session: Session): List[ShoppingList] = ShoppingList.table.list

  def find(id: Int)(implicit session: Session): Option[(ShoppingList, Seq[ShoppingListItem])] = ???

}
