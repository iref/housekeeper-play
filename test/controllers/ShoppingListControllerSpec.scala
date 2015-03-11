package controllers

import models.{ShoppingList, ShoppingListRepository}
import org.specs2.mock.Mockito
import play.api.db.DB
import play.api.db.slick.Config.driver.simple._
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

class ShoppingListControllerSpec extends PlaySpecification with Mockito {

  val shoppingListRepository = mock[ShoppingListRepository]

  "ShoppingListController#index" should {
    "render shopping list template" in new WithApplication {
      // given
      val controller = new ShoppingListController(shoppingListRepository)
      shoppingListRepository.all(any[Session]) returns List()

      // when
      val result = controller.index()(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Shopping Lists")
    }

    "render info message if there aren't any shopping lists" in new WithApplication {
      // given
      val controller = new ShoppingListController(shoppingListRepository)
      shoppingListRepository.all(any[Session]) returns List()

      // when
      val result = controller.index()(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("No shopping lists were created yet.")
    }

    "render all shopping lists" in new WithApplication {
      // given
      val controller = new ShoppingListController(shoppingListRepository)
      val shoppingLists = List(
        ShoppingList("First list", "Newbie list", Some(1)),
        ShoppingList("Second list", "My awesome list", Some(2)))
      shoppingListRepository.all(any[Session]) returns shoppingLists

      // when
      val result = controller.index()(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must not contain("No shopping lists were created yet.")
      shoppingLists.foreach { sl =>
        contentAsString(result) must contain(sl.title)
      }
    }
  }
}
