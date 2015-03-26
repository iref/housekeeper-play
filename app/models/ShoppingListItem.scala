package models

import play.api.db.slick.Config.driver.simple._

case class ShoppingListItem(name: String, quantity: Int, priceForOne: Option[BigDecimal] = None,
                            shoppingListId: Option[Int] = None, id: Option[Int] = None)

object ShoppingListItem {
  class ShoppingListItemTable(tag: Tag) extends Table[ShoppingListItem](tag, "shopping_list_items") {
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)
    def quantity = column[Int]("quantity", O.NotNull)
    def pricePerItem = column[Option[BigDecimal]]("price_for_one", O.Nullable)
    def shoppingListId = column[Option[Int]]("shopping_list_id", O.NotNull)

    def * = (name, quantity, pricePerItem, shoppingListId, id) <> ((ShoppingListItem.apply _).tupled, ShoppingListItem.unapply)

    def shoppingListFK = foreignKey("shopping_item_list_fk", shoppingListId, ShoppingList.table)(_.id)
  }

  val table = TableQuery[ShoppingListItemTable]
}

class ShoppingListItemRepository {

  def add(id: Int, item: ShoppingListItem)(implicit session: Session): ShoppingListItem = {
    val withListId = item.copy(shoppingListId = Some(id))
    val insertWithId = (ShoppingListItem.table returning ShoppingListItem.table.map(_.id))
      .into((_, id) => withListId.copy(id = id))
    insertWithId += withListId
  }

  def remove(id: Int)(implicit session: Session): Unit = {
    ShoppingListItem.table.filter(_.id === id).delete
  }

  def update(item: ShoppingListItem)(implicit session: Session): Unit = {
    ShoppingListItem.table.filter(_.id === item.id).update(item)
  }

  def find(id: Int)(implicit session: Session): Option[ShoppingListItem] = {
    ShoppingListItem.table.filter(_.id === id).firstOption
  }
}