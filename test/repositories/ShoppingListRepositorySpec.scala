package repositories

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import models.{ShoppingList, ShoppingListItem}
import test.HousekeeperSpec

class ShoppingListRepositorySpec extends HousekeeperSpec with Database {

  val shoppingListA = ShoppingList("First shopping list", Some("My first awesome shopping list"))

  val shoppingLists = Vector(
    shoppingListA,
    ShoppingList("Second awesome list", Some("Even more awesome list"))
  )

  def withShoppingListRepo(f: ShoppingListRepository => Unit) = {
    withDatabase(repos => f(repos.shoppingListRepository))
  }

  "ShoppingListRepository" should {

    "get all shopping lists" in withShoppingListRepo { shoppingListRepository =>
      // given
      val saveLists = shoppingLists.map(sl => shoppingListRepository.save(sl))
      Future.sequence(saveLists).futureValue

      // when
      val allLists = shoppingListRepository.all.futureValue

      // then
      allLists should have size (2)
    }

    "get empty list if no shopping list was created" in withShoppingListRepo { shoppingListRepository =>
      // when
      val shoppingLists = shoppingListRepository.all.futureValue

      // then
      shoppingLists should be(empty)
    }

    "get shopping list and its items by id" in withDatabase { repos =>
      // given
      val Some(shoppingListId) = repos.shoppingListRepository.save(shoppingListA).futureValue.id

      val items = Vector(
        ShoppingListItem("Macbook Pro 13'", 1, Some(1299.00)),
        ShoppingListItem("Brewdog 5am Saint Red Ale", 6, Some(5.0))
      ).map(i => repos.shoppingListItemRepository.add(shoppingListId, i))
      Future.sequence(items).futureValue

      // when
      val Some(sld) = repos.shoppingListRepository.find(shoppingListId).futureValue

      // then
      sld.shoppingList.id should be(Some(shoppingListId))
      sld.shoppingList.title should be(shoppingListA.title)
      sld.shoppingList.description should be(shoppingListA.description)
      sld.items should have size (2)
    }

    "find None for nonexistent shopping list id" in withShoppingListRepo { shoppingListRepository =>
      // given
      val shoppingListId = 1

      // when
      val notFound = shoppingListRepository.find(shoppingListId).futureValue

      // then
      notFound should be(None)
    }

    "save newList shopping list" in withShoppingListRepo { shoppingListRepository =>
      // given
      val newShoppingList = ShoppingList("New awesome list", Some("The most awesome shopping list"))

      // when
      val stored = shoppingListRepository.save(newShoppingList).futureValue

      // then
      stored.id should not be (None)
      val found = shoppingListRepository.find(stored.id.get).futureValue
      found match {
        case None         => fail("Shopping list was not saved.")
        case Some(detail) => detail.shoppingList should be(stored)
      }
    }

    "remove existing shopping list" in withShoppingListRepo { shoppingListRepository =>
      // given
      val Some(id) = shoppingListRepository.save(shoppingListA).futureValue.id

      // when
      shoppingListRepository.remove(id).futureValue

      // then
      val notFound = shoppingListRepository.find(id).futureValue
      notFound should be(None)
    }

    "remove nonexistent list keeps every list in database" in withShoppingListRepo { shoppingListRepository =>
      // given
      val saveLists = shoppingLists.map(sl => shoppingListRepository.save(sl))
      Future.sequence(saveLists).futureValue

      // when
      shoppingListRepository.remove(Int.MaxValue).futureValue

      // then
      val allLists = shoppingListRepository.all.futureValue
      allLists should have size (2)
    }

    "update existing shopping list in database" in withShoppingListRepo { shoppingListRepository =>
      // given
      val Some(id) = shoppingListRepository.save(shoppingListA).futureValue.id
      val toUpdate = ShoppingList("New test title", Some("Super duper updated description"), Some(id))

      // when
      shoppingListRepository.update(toUpdate).futureValue

      // then
      val updated = shoppingListRepository.find(id).futureValue
      updated match {
        case None         => fail("Shopping list wasn't updated.")
        case Some(detail) => detail.shoppingList should be(toUpdate)
      }
    }

    "update does not update other shopping lists" in withShoppingListRepo { shoppingListRepository =>
      //given
      shoppingListRepository.save(shoppingListA).futureValue
      val toUpdate = ShoppingList("New title of nonexisting list", Some("New description of nonexisting list"), Some(Int.MaxValue))

      // when
      shoppingListRepository.update(toUpdate).futureValue

      // then
      val all = shoppingListRepository.all.futureValue
      all should have size (1)

      val storedList = all.head
      storedList.title should be(shoppingListA.title)
      storedList.description should be(shoppingListA.description)
    }

    "not update shopping list without id" in withShoppingListRepo { shoppingListRepository =>
      // given
      shoppingListRepository.save(shoppingListA).futureValue
      val toUpdate = ShoppingList("New title of list without id", Some("New description of list without id"))

      // when
      shoppingListRepository.update(toUpdate).futureValue

      // then
      val all = shoppingListRepository.all.futureValue
      all should have size (1)

      val storedList = all.head
      storedList.title should be(shoppingListA.title)
      storedList.description should be(shoppingListA.description)
    }
  }
}
