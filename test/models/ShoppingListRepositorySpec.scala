package models

import org.specs2.mutable.Specification
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

class ShoppingListRepositorySpec extends Specification with Database {

  val shoppingListRepository = new ShoppingListRepository()

  "ShoppingListRepository" should {

    "get all shopping lists" in withDatabase { implicit session =>
      // given
      ShoppingList.table ++= Seq(
        ShoppingList("First shopping list", "My first awesome shopping list"),
        ShoppingList("Second awesome list", "Even more awesome list")
      )

      // when
      val shoppingLists = shoppingListRepository.all

      // then
      shoppingLists must have size(2)
    }

    "get empty list if no shopping list was created" in withDatabase { implicit session =>
      // when
      val shoppingLists = shoppingListRepository.all

      // then
      shoppingLists must beEmpty
    }

    "get shopping list and its items by id" in withDatabase { implicit session =>
      // given
      val shoppingList = ShoppingList("First shopping list", "My first awesome shopping list")
      val shoppingListId = (ShoppingList.table returning ShoppingList.table.map(_.id)).insert(shoppingList)

      val items = Seq(
        ShoppingListItem("Macbook Pro 13'", 1, Some(1299.00), shoppingListId = shoppingListId),
        ShoppingListItem("Brewdog 5am Saint Red Ale", 6, Some(5.0), shoppingListId = shoppingListId))
      ShoppingListItem.table ++= items

      // when
      val Some(sld) = shoppingListRepository.find(shoppingListId.get)

      // then
      sld.shoppingList.id must beEqualTo(shoppingListId)
      sld.shoppingList.title must beEqualTo(shoppingList.title)
      sld.shoppingList.description must beEqualTo(shoppingList.description)
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
      val newShoppingList = ShoppingList("New awesome list", "The most awesome shopping list")

      // when
      val stored = shoppingListRepository.save(newShoppingList)

      // then
      stored.id must not beNone
      val found = ShoppingList.table.filter(_.id === stored.id).firstOption
      found must beSome(stored)
    }

  }
}
