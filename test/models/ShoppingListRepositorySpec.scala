package models

import org.specs2.mutable.Specification
import play.api.db.slick.Config.driver.simple._

class ShoppingListRepositorySpec extends Specification with Database {

  val shoppingListRepository = new ShoppingListRepository()

  val shoppingListA = ShoppingList("First shopping list", Some("My first awesome shopping list"))

  val shoppingLists = Seq(
    shoppingListA,
    ShoppingList("Second awesome list", Some("Even more awesome list"))
  )

  "ShoppingListRepository" should {

    "get all shopping lists" in withDatabase { implicit session =>
      // given
      ShoppingList.table ++= shoppingLists

      // when
      val allLists = shoppingListRepository.all

      // then
      allLists must have size(2)
    }

    "get empty list if no shopping list was created" in withDatabase { implicit session =>
      // when
      val shoppingLists = shoppingListRepository.all

      // then
      shoppingLists must beEmpty
    }

    "get shopping list and its items by id" in withDatabase { implicit session =>
      // given
      val shoppingListId = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingListA

      val items = Seq(
        ShoppingListItem("Macbook Pro 13'", 1, Some(1299.00), shoppingListId = shoppingListId),
        ShoppingListItem("Brewdog 5am Saint Red Ale", 6, Some(5.0), shoppingListId = shoppingListId))
      ShoppingListItem.table ++= items

      // when
      val Some(sld) = shoppingListRepository.find(shoppingListId.get)

      // then
      sld.shoppingList.id must beEqualTo(shoppingListId)
      sld.shoppingList.title must beEqualTo(shoppingListA.title)
      sld.shoppingList.description must beEqualTo(shoppingListA.description)
      sld.items must have size(2)
    }

    "find None for nonexistent shopping list id" in withDatabase { implicit session =>
      // given
      val shoppingListId = 1

      // when
      val notFound = shoppingListRepository.find(shoppingListId)

      // then
      notFound should beNone
    }

    "save newList shopping list" in withDatabase { implicit session =>
      // given
      val newShoppingList = ShoppingList("New awesome list", Some("The most awesome shopping list"))

      // when
      val stored = shoppingListRepository.save(newShoppingList)

      // then
      stored.id must not beNone
      val found = ShoppingList.table.filter(_.id === stored.id).firstOption
      found must beSome(stored)
    }

    "remove existing shopping list" in withDatabase { implicit session =>
      // given
      val Some(id) = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingListA

      // when
      shoppingListRepository.remove(id)

      // then
      val notFound = ShoppingList.table.filter(_.id === id).firstOption
      notFound must beNone
    }

    "remove nonexistent list keeps every list in database" in withDatabase { implicit session =>
      // given
      (ShoppingList.table returning ShoppingList.table.map(_.id)) ++= shoppingLists

      // when
      shoppingListRepository.remove(Int.MaxValue)

      // then
      val allLists = ShoppingList.table.list
      allLists must have size(2)
    }

    "update existing shopping list in database" in withDatabase { implicit session =>
      // given
      val Some(id) = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingListA
      val toUpdate = ShoppingList("New test title", Some("Super duper updated description"), Some(id))

      // when
      shoppingListRepository.update(toUpdate)

      // then
      val updated = ShoppingList.table.filter(_.id === id).firstOption
      updated must beSome(toUpdate)
    }

    "update does not update other shopping lists" in withDatabase { implicit session =>
      //given
      ShoppingList.table += shoppingListA
      val toUpdate = ShoppingList("New title of nonexisting list", Some("New description of nonexisting list"), Some(Int.MaxValue))

      // when
      shoppingListRepository.update(toUpdate)

      // then
      val all = ShoppingList.table.list
      all must have size(1)

      val storedList = all.head
      storedList.title must beEqualTo(shoppingListA.title)
      storedList.description must beEqualTo(shoppingListA.description)
    }

    "not update shopping list without id" in withDatabase { implicit session =>
      // given
      ShoppingList.table += shoppingListA
      val toUpdate = ShoppingList("New title of list without id", Some("New description of list without id"))

      // when
      shoppingListRepository.update(toUpdate)

      // then
      val all = ShoppingList.table.list
      all must have size(1)

      val storedList = all.head
      storedList.title must beEqualTo(shoppingListA.title)
      storedList.description must beEqualTo(shoppingListA.description)
    }
  }
}
