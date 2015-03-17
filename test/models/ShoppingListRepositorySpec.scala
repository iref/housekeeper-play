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
      val shoppingListId = (ShoppingList.table returning ShoppingList.table.map(_.id)).insert(shoppingListA)

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

    "save new shopping list" in withDatabase { implicit session =>
      // given
      val newShoppingList = ShoppingList("New awesome list", Some("The most awesome shopping list"))

      // when
      val stored = shoppingListRepository.save(newShoppingList)

      // then
      stored.id must not beNone
      val found = ShoppingList.table.filter(_.id === stored.id).firstOption
      found must beSome(stored)
    }

    "add item to list" in withDatabase { implicit session =>
      // given
      val Some(id) = (ShoppingList.table returning ShoppingList.table.map(_.id)) += shoppingListA
      val item = ShoppingListItem("Super item", 1, Some(12.0), Some(id))

      // when
      val returnedItem = shoppingListRepository.addItem(id, item)

      // then
      returnedItem.id must beSome

      val Some(shoppingListDetail) = shoppingListRepository.find(id)
      shoppingListDetail.items must have size(1)

      val storedItem = shoppingListDetail.items(0)
      storedItem.id must beSome(returnedItem.id.get)
      storedItem.name must beEqualTo("Super item")
      storedItem.quantity must beEqualTo(1)
      storedItem.priceForOne must beSome(12.0)
      storedItem.shoppingListId must beSome(id)
    }

  }
}
