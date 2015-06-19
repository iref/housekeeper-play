package models

case class ShoppingList(title: String, description: Option[String] = None, id: Option[Int] = None)

case class ShoppingListDetail(shoppingList: ShoppingList, items: Seq[ShoppingListItem])


