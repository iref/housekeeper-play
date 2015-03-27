package models

import org.specs2.mutable.Specification
import play.api.db.slick.Config.driver.simple._

/**
 * Created by ferko on 26.3.15.
 */
class ShoppingListItemRepositorySpec extends Specification with Database {

  val shoppingListRepository = new ShoppingListItemRepository

  val shoppingList = ShoppingList("First shopping list", Some("My first awesome shopping list"))

  "add item to list" in withDatabase { implicit session =>
    // given
    val Some(id) = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingList
    val item = ShoppingListItem("Super item", 1, Some(12.0), Some(id))

    // when
    val returnedItem = shoppingListRepository.add(id, item)

    // then
    returnedItem.id must beSome

    val Some(storedItem) = shoppingListRepository.find(returnedItem.id.get)

    storedItem.id must beSome(returnedItem.id.get)
    storedItem.name must beEqualTo("Super item")
    storedItem.quantity must beEqualTo(1)
    storedItem.priceForOne must beSome(12.0)
    storedItem.shoppingListId must beSome(id)
  }

  "remove item from list" in withDatabase { implicit session =>
    // given
    val id = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingList
    val item = ShoppingListItem("Super item", 1, Some(12.0), id)
    val Some(itemId) = (ShoppingListItem.table returning ShoppingListItem.table.map(_.id)) += item

    // when
    shoppingListRepository.remove(itemId)

    // then
    shoppingListRepository.find(itemId) must beNone
  }

  "not remove any item if id does not exist" in withDatabase { implicit session =>
    // given
    val id = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingList
    val item = ShoppingListItem("Super item", 1, Some(12.0), id)
    val Some(itemId) = (ShoppingListItem.table returning ShoppingListItem.table.map(_.id)) += item

    // when
    shoppingListRepository.remove(itemId + 1)

    // then
    val Some(storedItem) = shoppingListRepository.find(itemId)
    storedItem must beEqualTo(storedItem)
  }

  "newList existing item" in withDatabase { implicit session =>
    // given
    val id = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingList
    val item = ShoppingListItem("Super item", 1, Some(12.0), id)
    val Some(itemId) = (ShoppingListItem.table returning ShoppingListItem.table.map(_.id)) += item
    val updated = item.copy(name = "Updated item name", quantity = 2, id = Some(itemId))

    // when
    shoppingListRepository.update(updated)

    // then
    val Some(foundItem) = shoppingListRepository.find(itemId)
    foundItem must beEqualTo(updated)
  }

  "find existing item by id" in withDatabase { implicit session =>
    // given
    val id = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingList
    val item = ShoppingListItem("Super item", 1, Some(12.0), id)
    val Some(itemId) = (ShoppingListItem.table returning ShoppingListItem.table.map(_.id)) += item

    // when
    val Some(found) = shoppingListRepository.find(itemId)

    // then
    found.id must beSome(itemId)
    found.name must beEqualTo("Super item")
    found.priceForOne must beSome(12.0)
    found.quantity must beEqualTo(1)
    found.shoppingListId must beEqualTo(id)
  }

  "return None if item with id does not exist" in withDatabase { implicit session =>
    // given
    val id = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingList
    val item = ShoppingListItem("Super item", 1, Some(12.0), id)
    val Some(itemId) = (ShoppingListItem.table returning ShoppingListItem.table.map(_.id)) += item

    // when
    val notFound = shoppingListRepository.find(Int.MaxValue)

    // then
    notFound must beNone
  }

}
