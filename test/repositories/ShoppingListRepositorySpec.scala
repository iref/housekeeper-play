package repositories

import models.{ShoppingList, ShoppingListItem}
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ShoppingListRepositorySpec extends Specification {

  val shoppingListA = ShoppingList("First shopping list", Some("My first awesome shopping list"))

  val shoppingLists = Vector(
    shoppingListA,
    ShoppingList("Second awesome list", Some("Even more awesome list"))
  )

  "ShoppingListRepository" should {

    "get all shopping lists" in new Database {
      // given
      val saveLists = shoppingLists.map(sl => shoppingListRepository.save(sl))
      Await.ready(Future.sequence(saveLists), 1.second)

      // when
      val allLists = Await.result(shoppingListRepository.all, 1.second)

      // then
      allLists must have size(2)
    }

    "get empty list if no shopping list was created" in new Database {
      // when
      val shoppingLists = Await.result(shoppingListRepository.all, 1.second)

      // then
      shoppingLists must beEmpty
    }

    "get shopping list and its items by id" in new Database {
      // given
      val Some(shoppingListId) = Await.result(shoppingListRepository.save(shoppingListA), 1.second).id

      val items = Vector(
        ShoppingListItem("Macbook Pro 13'", 1, Some(1299.00)),
        ShoppingListItem("Brewdog 5am Saint Red Ale", 6, Some(5.0))
      ).map(i => shoppingListItemRepository.add(shoppingListId, i))
      Await.ready(Future.sequence(items), 1.second)

      // when
      val Some(sld) = Await.result(shoppingListRepository.find(shoppingListId), 1.second)

      // then
      sld.shoppingList.id must beSome(shoppingListId)
      sld.shoppingList.title must beEqualTo(shoppingListA.title)
      sld.shoppingList.description must beEqualTo(shoppingListA.description)
      sld.items must have size(2)
    }

    "find None for nonexistent shopping list id" in new Database {
      // given
      val shoppingListId = 1

      // when
      val notFound = Await.result(shoppingListRepository.find(shoppingListId), 1.second)

      // then
      notFound should beNone
    }

    "save newList shopping list" in new Database {
      // given
      val newShoppingList = ShoppingList("New awesome list", Some("The most awesome shopping list"))

      // when
      val stored = Await.result(shoppingListRepository.save(newShoppingList), 1.second)

      // then
      stored.id must not beNone
      val found = Await.result(shoppingListRepository.find(stored.id.get), 1.second)
      found match {
        case None => failure("Shopping list was not saved.")
        case Some(detail) => detail.shoppingList must beEqualTo(stored)
      }
    }

    "remove existing shopping list" in new Database {
      // given
      val Some(id) = Await.result(shoppingListRepository.save(shoppingListA), 1.second).id

      // when
      Await.ready(shoppingListRepository.remove(id), 1.second)

      // then
      val notFound = Await.result(shoppingListRepository.find(id), 1.second)
      notFound must beNone
    }

    "remove nonexistent list keeps every list in database" in new Database {
      // given
      val saveLists = shoppingLists.map(sl => shoppingListRepository.save(sl))
      Await.ready(Future.sequence(saveLists), 1.second)

      // when
      Await.ready(shoppingListRepository.remove(Int.MaxValue), 1.second)

      // then
      val allLists = Await.result(shoppingListRepository.all, 1.second)
      allLists must have size(2)
    }

    "update existing shopping list in database" in new Database {
      // given
      val Some(id) = Await.result(shoppingListRepository.save(shoppingListA), 1.second).id
      val toUpdate = ShoppingList("New test title", Some("Super duper updated description"), Some(id))

      // when
      Await.ready(shoppingListRepository.update(toUpdate), 1.second)

      // then
      val updated = Await.result(shoppingListRepository.find(id), 1.second)
      updated match {
        case None => failure("Shopping list wasn't updated.")
        case Some(detail) => detail.shoppingList must beEqualTo(toUpdate)
      }
    }

    "update does not update other shopping lists" in new Database {
      //given
      Await.ready(shoppingListRepository.save(shoppingListA), 1.second)
      val toUpdate = ShoppingList("New title of nonexisting list", Some("New description of nonexisting list"), Some(Int.MaxValue))

      // when
      Await.ready(shoppingListRepository.update(toUpdate), 1.second)

      // then
      val all = Await.result(shoppingListRepository.all, 1.second)
      all must have size(1)

      val storedList = all.head
      storedList.title must beEqualTo(shoppingListA.title)
      storedList.description must beEqualTo(shoppingListA.description)
    }

    "not update shopping list without id" in new Database {
      // given
      Await.ready(shoppingListRepository.save(shoppingListA), 1.second)
      val toUpdate = ShoppingList("New title of list without id", Some("New description of list without id"))

      // when
      Await.ready(shoppingListRepository.update(toUpdate), 1.second)

      // then
      val all = Await.result(shoppingListRepository.all, 1.second)
      all must have size(1)

      val storedList = all.head
      storedList.title must beEqualTo(shoppingListA.title)
      storedList.description must beEqualTo(shoppingListA.description)
    }
  }
}
