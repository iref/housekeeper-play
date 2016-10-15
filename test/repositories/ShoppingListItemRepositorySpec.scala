package repositories

import models.{ShoppingList, ShoppingListItem}
import test.HousekeeperSpec

class ShoppingListItemRepositorySpec extends HousekeeperSpec with Database {

  val shoppingList = ShoppingList("First shopping list", Some("My first awesome shopping list"))

  "ShoppingListItemRepository" should {
    "add item to list" in {
      // given
      val Some(id) = shoppingListRepository.save(shoppingList).futureValue.id
      val item = ShoppingListItem("Super item", 1, Some(12.0))

      // when
      val returnedItem = shoppingListItemRepository.add(id, item).futureValue

      // then
      returnedItem.id shouldBe defined

      val Some(storedItem) = shoppingListItemRepository.find(returnedItem.id.get).futureValue

      storedItem.id should be (Some(returnedItem.id.get))
      storedItem.name should be("Super item")
      storedItem.quantity should be(1)
      storedItem.priceForOne should be(Some(12.0))
      storedItem.shoppingListId should be (Some(id))
    }

    "remove item from list" in {
      // given
      val Some(id) = shoppingListRepository.save(shoppingList).futureValue.id
      val item = ShoppingListItem("Super item", 1, Some(12.0))
      val Some(itemId) = shoppingListItemRepository.add(id, item).futureValue.id

      // when
      shoppingListItemRepository.remove(itemId).futureValue

      // then
      val notFound = shoppingListItemRepository.find(itemId).futureValue
      notFound should be(None)
    }

    "not remove any item if id does not exist" in {
      // given
      val Some(id) = shoppingListRepository.save(shoppingList).futureValue.id
      val item = ShoppingListItem("Super item", 1, Some(12.0))
      val Some(itemId) = shoppingListItemRepository.add(id, item).futureValue.id

      // when
      shoppingListItemRepository.remove(itemId + 1).futureValue

      // then
      val Some(storedItem) = shoppingListItemRepository.find(itemId).futureValue
      storedItem should be(storedItem)
    }

    "update existing item" in {
      // given
      val Some(id) = shoppingListRepository.save(shoppingList).futureValue.id
      val item = ShoppingListItem("Super item", 1, Some(12.0))
      val Some(itemId) = shoppingListItemRepository.add(id, item).futureValue.id
      val updated = item.copy(name = "Updated item name", quantity = 2, shoppingListId = Some(id), id = Some(itemId))

      // when
      shoppingListItemRepository.update(updated).futureValue

      // then
      val Some(foundItem) = shoppingListItemRepository.find(itemId).futureValue
      foundItem should be(updated)
    }

    "not update any other item" in {
      // given
      val Some(id) = shoppingListRepository.save(shoppingList).futureValue.id
      val item = ShoppingListItem("Super item", 1, Some(12.0))
      val Some(itemId) = shoppingListItemRepository.add(id, item).futureValue.id
      val item2 = ShoppingListItem("Super item 2", 1, Some(10.0), Some(id))
      val Some(itemId2) = shoppingListItemRepository.add(id, item2).futureValue.id
      val updated = item.copy(name = "Updated item name", quantity = 2, shoppingListId = Some(id), id = Some(itemId))

      // when
      shoppingListItemRepository.update(updated).futureValue

      // then
      val Some(foundItem) = shoppingListItemRepository.find(itemId2).futureValue
      foundItem should be(item2.copy(id = Some(itemId2)))
    }

    "find existing item by id" in {
      // given
      val Some(id) = shoppingListRepository.save(shoppingList).futureValue.id
      val item = ShoppingListItem("Super item", 1, Some(12.0))
      val Some(itemId) = shoppingListItemRepository.add(id, item).futureValue.id

      // when
      val Some(found) = shoppingListItemRepository.find(itemId).futureValue

      // then
      found.id should be(Some(itemId))
      found.name should be("Super item")
      found.priceForOne should be(Some(12.0))
      found.quantity should be(1)
      found.shoppingListId should be(Some(id))
    }

    "return None if item with id does not exist" in {
      // given
      val Some(id) = shoppingListRepository.save(shoppingList).futureValue.id
      val item = ShoppingListItem("Super item", 1, Some(12.0))
      val Some(itemId) = shoppingListItemRepository.add(id, item).futureValue.id

      // when
      val notFound = shoppingListItemRepository.find(Int.MaxValue).futureValue

      // then
      notFound should be(None)
    }
  }

}
