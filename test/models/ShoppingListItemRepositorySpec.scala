package models

import org.specs2.mutable.Specification
import play.api.Application
import play.api.test.WithApplicationLoader


import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by ferko on 26.3.15.
 */
class ShoppingListItemRepositorySpec extends Specification {

  val shoppingList = ShoppingList("First shopping list", Some("My first awesome shopping list"))

  trait WithRepository extends WithApplicationLoader {
    private val app2Repository = Application.instanceCache[ShoppingListItemRepositoryImpl]
    val repository = app2Repository(app)

    private val app2ShoppingListRepository = Application.instanceCache[ShoppingListRepositoryImpl]
    val shoppingListRepository = app2ShoppingListRepository(app)
  }

  "add item to list" in new WithRepository {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))

    // when
    val returnedItem = Await.result(repository.add(id, item), 1.second)

    // then
    returnedItem.id must beSome

    val Some(storedItem) = Await.result(repository.find(returnedItem.id.get), 1.second)

    storedItem.id must beSome(returnedItem.id.get)
    storedItem.name must beEqualTo("Super item")
    storedItem.quantity must beEqualTo(1)
    storedItem.priceForOne must beSome(12.0)
    storedItem.shoppingListId must beSome(id)
  }

  "remove item from list" in new WithRepository {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(repository.add(id, item), 1.second).id

    // when
    Await.ready(repository.remove(itemId), 1.second)

    // then
    val notFound = Await.result(repository.find(itemId), 1.second)
    notFound must beNone
  }

  "not remove any item if id does not exist" in new WithRepository {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(repository.add(id, item), 1.second).id

    // when
    Await.ready(shoppingListRepository.remove(itemId + 1), 1.second)

    // then
    val Some(storedItem) = Await.result(shoppingListRepository.find(itemId), 1.second)
    storedItem must beEqualTo(storedItem)
  }

  "newList existing item" in new WithRepository {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(repository.add(id, item), 1.second).id
    val updated = item.copy(name = "Updated item name", quantity = 2, id = Some(itemId))

    // when
    Await.ready(repository.update(updated), 1.second)

    // then
    val Some(foundItem) = Await.result(shoppingListRepository.find(itemId), 1.second)
    foundItem must beEqualTo(updated)
  }

  "find existing item by id" in new WithRepository {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(repository.add(id, item), 1.second).id

    // when
    val Some(found) = Await.result(repository.find(itemId), 1.second)

    // then
    found.id must beSome(itemId)
    found.name must beEqualTo("Super item")
    found.priceForOne must beSome(12.0)
    found.quantity must beEqualTo(1)
    found.shoppingListId must beEqualTo(id)
  }

  "return None if item with id does not exist" in new WithRepository {
    // given
    val Some(id) = Await.result(shoppingListRepository.save(shoppingList), 1.second).id
    val item = ShoppingListItem("Super item", 1, Some(12.0))
    val Some(itemId) = Await.result(repository.add(id, item), 1.second).id

    // when
    val notFound = Await.result(shoppingListRepository.find(Int.MaxValue), 1.second)

    // then
    notFound must beNone
  }

}
