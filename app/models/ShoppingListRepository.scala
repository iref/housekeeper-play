package models

import play.api.db.slick.Config.driver.simple._

case class ShoppingList(title: String, description: Option[String] = None, id: Option[Int] = None)

case class ShoppingListDetail(shoppingList: ShoppingList, items: List[ShoppingListItem])

object ShoppingList {
  class ShoppingListsTable(tag: Tag) extends Table[ShoppingList](tag, "shopping_lists") {
    def title = column[String]("title", O.NotNull)
    def description = column[Option[String]]("description", O.Nullable)
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)

    def * = (title, description, id) <> ((ShoppingList.apply _).tupled, ShoppingList.unapply)
  }

  val table = TableQuery[ShoppingListsTable]
}

class ShoppingListRepository {

  def all(implicit session: Session): List[ShoppingList] = ShoppingList.table.list

  def find(id: Int)(implicit session: Session): Option[ShoppingListDetail] = {
    val list = ShoppingList.table.filter(_.id === id).firstOption
    val items = ShoppingListItem.table.filter(_.shoppingListId === id).list
    list.map(sl => ShoppingListDetail(sl, items))
  }

  def save(shoppingList: ShoppingList)(implicit session: Session): ShoppingList = {
    val insertWithId = (ShoppingList.table returning ShoppingList.table.map(_.id))
      .into((_, id) => shoppingList.copy(id = id))
    insertWithId += shoppingList
  }

  def addItem(id: Int, item: ShoppingListItem)(implicit session: Session): ShoppingListItem = {
    val withListId = item.copy(shoppingListId = Some(id))
    val insertWithId = (ShoppingListItem.table returning ShoppingListItem.table.map(_.id))
      .into((_, id) => withListId.copy(id = id))
    insertWithId += withListId
  }

}
