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
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        ShoppingList("Second list", Some("My awesome list"), Some(2)))
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

  "ShoppingListController#show" should {

    "render shopping list detail template" in new WithApplication {
      // given
      shoppingListRepository.find(any[Int])(any[Session]) returns(None)

      // when
      val result = controller.show(1)(FakeRequest())

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
        ShoppingList("First list", Some("Newbie list"), Some(1)),
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
      contentAsString(result) must contain(shoppingListDetail.shoppingList.description.get)
      shoppingListDetail.items.foreach { item =>
        contentAsString(result) must contain(item.name)
        contentAsString(result) must contain(item.quantity.toString)
        contentAsString(result) must contain(item.priceForOne.map(_.toString).getOrElse("-"))
      }
    }
  }

  "ShoppingListController#new" should {
    "render edit template" in {
      // when
      val result = controller.newList()(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("New Shopping List")
    }
  }

  "ShoppingListController#save" should {
    "not save shopping list without title" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("description", "testdescription"))

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"title_error")
    }

    "redirect with error message if saving fails" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("title", "Test"), ("description", "Test description"))
      val newShoppingList = ShoppingList("Test", Some("Test description"))
      shoppingListRepository.save(org.mockito.Matchers.eq(newShoppingList))(any[Session]) returns(newShoppingList)

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("error") must beSome
    }

    "save valid shopping list" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("description", "testdescription"), ("title", "test"))
      val expectedShoppingList = ShoppingList("test", Some("testdescription"))
      shoppingListRepository.save(org.mockito.Matchers.eq(expectedShoppingList))(any[Session]) returns(expectedShoppingList.copy(id = Some(1)))

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }
}
