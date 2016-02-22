package models

case class ShoppingListItem(name: String,
                            quantity: Int,
                            priceForOne: Option[BigDecimal] = None,
                            shoppingListId: Option[Int] = None,
                            id: Option[Int] = None)

