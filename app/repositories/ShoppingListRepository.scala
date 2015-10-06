package repositories

import models.{ShoppingListDetail, ShoppingList}

import scala.concurrent.Future

/**
 * Trait for accessing and manipulating shopping lists in data storage.
 */
trait ShoppingListRepository {

  def all: Future[List[ShoppingList]]

  def find(id: Int): Future[Option[ShoppingListDetail]]

  def save(shoppingList: ShoppingList): Future[ShoppingList]

  def update(shoppingList: ShoppingList): Future[Int]

  def remove(id: Int): Future[Int]

}
