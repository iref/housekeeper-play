package models

import org.specs2.mutable.Specification
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

class ShoppingListSpec extends Specification with Database {

  "ShoppingList" should {

    "get all shopping lists" in withDatabase { implicit session =>
      // given
      ShoppingList.table ++= Seq(
        ShoppingList("First shopping list", "My first awesome shopping list"),
        ShoppingList("Second awesome list", "Even more awesome list")
      )

      // when
      val shoppingLists = ShoppingList.all

      // then
      shoppingLists must have size(2)
    }

    "get empty list if no shopping list was created" in withDatabase { implicit session =>
      // when
      val shoppingLists = ShoppingList.all

      // then
      shoppingLists must beEmpty
    }

  }
}