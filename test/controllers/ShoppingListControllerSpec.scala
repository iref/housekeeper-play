package controllers

import models.{ShoppingListItem, ShoppingListDetail, ShoppingList, ShoppingListRepository}
import org.specs2.mock.Mockito
import play.api.db.DB
import play.api.db.slick.Config.driver.simple._
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

class ShoppingListControllerSpec extends PlaySpecification with Mockito {

  val shoppingListRepository = mock[ShoppingListRepository]
  val controller = new ShoppingListController(shoppingListRepository)

  "ShoppingListController#index" should {
    "render shopping list template" in new WithApplication {
      // given
      shoppingListRepository.all(any[Session]) returns List()

      // when
      val result = controller.index()(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Shopping Lists")
    }

    "render info message if there aren't any shopping lists" in new WithApplication {
      // given
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

  "ShoppingList#show" should {

    "render shopping list detail template" in new WithApplication {
      // when
      val result = controller.show(1)(FakeRequest())
      shoppingListRepository.find(any[Int])(any[Session]) returns(None)

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping List Detail | Housekeeper")
    }

    "render message if shopping detail doesn't exists" in new WithApplication {
      // given
      shoppingListRepository.find(any[Int])(any[Session]) returns(None)

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping list was not found.")
    }

    "render shopping list detail" in new WithApplication {
      // given
      val shoppingListDetail = ShoppingListDetail(
        ShoppingList("First list", "Newbie list", Some(1)),
        List(
          ShoppingListItem("Brewdog 5am Saint Red Ale", 1, None, Some(1), Some(1)),
          ShoppingListItem("Macbook Air 13", 1, Some(1000.0), Some(1), Some(2))
        )
      )
      shoppingListRepository.find(org.mockito.Matchers.eq(1))(any[Session]) returns Some(shoppingListDetail)

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain(shoppingListDetail.shoppingList.title)
      contentAsString(result) must contain(shoppingListDetail.shoppingList.description)
      shoppingListDetail.items.foreach { item =>
        contentAsString(result) must contain(item.name)
        contentAsString(result) must contain(item.quantity.toString)
        contentAsString(result) must contain(item.priceForOne.map(_.toString).getOrElse("-"))
      }
    }
  }
}