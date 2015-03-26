package models

/**
 * Trait provides repositories implementations.
 */
trait Repositories {

  lazy val shoppingListRepository = new ShoppingListRepository()
  
  lazy val shoppingListItemRepository = new ShoppingListItemRepository()

}
