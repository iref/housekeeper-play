package models

import org.specs2.mutable.Specification
import play.api.Application
import play.api.test.WithApplicationLoader

import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ShoppingListRepositorySpec extends Specification {

  val shoppingListA = ShoppingList("First shopping list", Some("My first awesome shopping list"))

  val shoppingLists = Seq(
    shoppingListA,
    ShoppingList("Second awesome list", Some("Even more awesome list"))
  )

  trait WithRepository extends WithApplicationLoader {
    private val app2Repository = Application.instanceCache[ShoppingListRepositoryImpl]
    val repository = app2Repository(app)

    private val app2shoppingListItemRepository = Application.instanceCache[ShoppingListItemRepositoryImpl]
    val sliRepository = app2shoppingListItemRepository(app)
  }

  "ShoppingListRepository" should {

    "get all shopping lists" in new WithRepository {
      // given
      val saveLists: Seq[Future[ShoppingList]] = shoppingLists.map(sl => repository.save(sl))
      Await.ready(Future.sequence(saveLists), 1.second)

      // when
      val allLists = Await.result(repository.all, 1.second)

      // then
      allLists must have size(2)
    }

    "get empty list if no shopping list was created" in new WithRepository {
      // when
      val shoppingLists = Await.result(repository.all, 1.second)

      // then
      shoppingLists must beEmpty
    }

    "get shopping list and its items by id" in new WithRepository {
      // given
      val Some(shoppingListId) = Await.result(repository.save(shoppingListA), 1.second).id

      val items = Seq(
        ShoppingListItem("Macbook Pro 13'", 1, Some(1299.00)),
        ShoppingListItem("Brewdog 5am Saint Red Ale", 6, Some(5.0))
      ).map(i => sliRepository.add(shoppingListId, i))
      Await.ready(Future.sequence(items), 1.second)

      // when
      val Some(sld) = Await.result(repository.find(shoppingListId), 1.second)

      // then
      sld.shoppingList.id must beEqualTo(shoppingListId)
      sld.shoppingList.title must beEqualTo(shoppingListA.title)
      sld.shoppingList.description must beEqualTo(shoppingListA.description)
      sld.items must have size(2)
    }

    "find None for nonexistent shopping list id" in new WithRepository {
      // given
      val shoppingListId = 1

      // when
      val notFound = Await.result(repository.find(shoppingListId), 1.second)

      // then
      notFound should beNone
    }

    "save newList shopping list" in new WithRepository {
      // given
      val newShoppingList = ShoppingList("New awesome list", Some("The most awesome shopping list"))

      // when
      val stored = Await.result(repository.save(newShoppingList), 1.second)

      // then
      stored.id must not beNone
      val found = Await.result(repository.find(stored.id.get), 1.second)
      found must beSome(stored)
    }

    "remove existing shopping list" in new WithRepository {
      // given
      val Some(id) = Await.result(repository.save(shoppingListA), 1.second).id

      // when
      Await.ready(repository.remove(id), 1.second)

      // then
      val notFound = Await.result(repository.find(id), 1.second)
      notFound must beNone
    }

    "remove nonexistent list keeps every list in database" in new WithRepository {
      // given
      val saveLists = shoppingLists.map(sl => repository.save(sl))
      Await.ready(Future.sequence(saveLists), 1.second)

      // when
      Await.ready(repository.remove(Int.MaxValue), 1.second)

      // then
      val allLists = Await.result(repository.all, 1.second)
      allLists must have size(2)
    }

    "update existing shopping list in database" in new WithRepository {
      // given
      val Some(id) = Await.result(repository.save(shoppingListA), 1.second).id
      val toUpdate = ShoppingList("New test title", Some("Super duper updated description"), Some(id))

      // when
      Await.ready(repository.update(toUpdate), 1.second)

      // then
      val Some(updated) = Await.result(repository.find(id), 1.second)
      updated.shoppingList must beEqualTo(toUpdate)
    }

    "update does not update other shopping lists" in new WithRepository {
      //given
      Await.ready(repository.save(shoppingListA), 1.second)
      val toUpdate = ShoppingList("New title of nonexisting list", Some("New description of nonexisting list"), Some(Int.MaxValue))

      // when
      Await.ready(repository.update(toUpdate), 1.second)

      // then
      val all = Await.result(repository.all, 1.second)
      all must have size(1)

      val storedList = all.head
      storedList.title must beEqualTo(shoppingListA.title)
      storedList.description must beEqualTo(shoppingListA.description)
    }

    "not update shopping list without id" in new WithRepository {
      // given
      Await.ready(repository.save(shoppingListA), 1.second)
      val toUpdate = ShoppingList("New title of list without id", Some("New description of list without id"))

      // when
      Await.ready(repository.update(toUpdate), 1.second)

      // then
      val all = Await.result(repository.all, 1.second)
      all must have size(1)

      val storedList = all.head
      storedList.title must beEqualTo(shoppingListA.title)
      storedList.description must beEqualTo(shoppingListA.description)
    }
  }
}
