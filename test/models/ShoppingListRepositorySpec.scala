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
      val Some((sl, is)) = shoppingListRepository.find(shoppingListId.get)

      // then
      sl.id must beEqualTo(shoppingListId.get)
      sl.title must beEqualTo(shoppingList.title)
      sl.description must beEqualTo(shoppingList.description)

      is must beEqualTo(items)
//      is.zip(items).foreach { case (actual, expected) =>
//        actual.name must beEqualTo(expected.name)
//        actual.quantity must beEqualTo(expected.quantity)
//        actual.priceForOne must beEqualTo(expected.priceForOne)
//        actual.shoppingListId must beEqualTo(shoppingListId)
//        actual.id must beSome
//      }
    }

  }
}
