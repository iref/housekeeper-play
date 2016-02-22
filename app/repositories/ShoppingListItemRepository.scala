package repositories


import scala.concurrent.Future

import models.ShoppingListItem

/**
 * Trait for accessing and manipulating shopping list items in data storage.
 */
trait ShoppingListItemRepository {

  def add(id: Int, item: ShoppingListItem): Future[ShoppingListItem]

  def remove(id: Int): Future[Int]

  def update(item: ShoppingListItem): Future[Int]

  def find(id: Int): Future[Option[ShoppingListItem]]
}
