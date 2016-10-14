package repositories

import models.{ShoppingList, ShoppingListItem}
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by ferko on 26.3.15.
 */
class ShoppingListItemRepositorySpec extends Specification {

  val shoppingList = ShoppingList("First shopping list", Some("My first awesome shopping list"))

  "add item to list" in new Database {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))

    // when
    val returnedItem = Await.result(shoppingListItemRepository.add(id, item), 1.second)

    // then
    returnedItem.id must beSome

    val Some(storedItem) = Await.result(shoppingListItemRepository.find(returnedItem.id.get), 1.second)

    storedItem.id must beSome(returnedItem.id.get)
    storedItem.name must beEqualTo("Super item")
    storedItem.quantity must beEqualTo(1)
    storedItem.priceForOne must beSome(12.0)
    storedItem.shoppingListId must beSome(id)
  }

  "remove item from list" in new Database {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(shoppingListItemRepository.add(id, item), 1.second).id

    // when
    Await.ready(shoppingListItemRepository.remove(itemId), 1.second)

    // then
    val notFound = Await.result(shoppingListItemRepository.find(itemId), 1.second)
    notFound must beNone
  }

  "not remove any item if id does not exist" in new Database {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(shoppingListItemRepository.add(id, item), 1.second).id

    // when
    Await.ready(shoppingListItemRepository.remove(itemId + 1), 1.second)

    // then
    val Some(storedItem) = Await.result(shoppingListItemRepository.find(itemId), 1.second)
    storedItem must beEqualTo(storedItem)
  }

  "update existing item" in new Database {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(shoppingListItemRepository.add(id, item), 1.second).id
    val updated = item.copy(name = "Updated item name", quantity = 2, shoppingListId = Some(id), id = Some(itemId))

    // when
    Await.ready(shoppingListItemRepository.update(updated), 1.second)

    // then
    val Some(foundItem) = Await.result(shoppingListItemRepository.find(itemId), 1.second)
    foundItem must beEqualTo(updated)
  }

  "not update any other item" in new Database {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(shoppingListItemRepository.add(id, item), 1.second).id
    val item2 = ShoppingListItem("Super item 2", 1, Some(10.0), Some(id))
    val Some(itemId2) = Await.result(shoppingListItemRepository.add(id, item2), 1.second).id
    val updated = item.copy(name = "Updated item name", quantity = 2, shoppingListId = Some(id), id = Some(itemId))

    // when
    Await.ready(shoppingListItemRepository.update(updated), 1.second)

    // then
    val Some(foundItem) = Await.result(shoppingListItemRepository.find(itemId2), 1.second)
    foundItem must beEqualTo(item2.copy(id = Some(itemId2)))
  }

  "find existing item by id" in new Database {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(shoppingListItemRepository.add(id, item), 1.second).id

    // when
    val Some(found) = Await.result(shoppingListItemRepository.find(itemId), 1.second)

    // then
    found.id must beSome(itemId)
    found.name must beEqualTo("Super item")
    found.priceForOne must beSome(12.0)
    found.quantity must beEqualTo(1)
    found.shoppingListId must beSome(id)
  }

  "return None if item with id does not exist" in new Database {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(shoppingListItemRepository.add(id, item), 1.second).id

    // when
    val notFound = Await.result(shoppingListItemRepository.find(Int.MaxValue), 1.second)

    // then
    notFound must beNone
  }

}
