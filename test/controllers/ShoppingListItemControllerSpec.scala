package controllers

import models.{ShoppingList, ShoppingListDetail, ShoppingListItem, ShoppingListRepository}
import org.mockito.Matchers
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.db.slick.Config.driver.simple._
import play.api.test.{WithApplication, FakeRequest, PlaySpecification}

/**
 * Created by ferko on 24.3.15.
 */
class ShoppingListItemControllerSpec extends PlaySpecification with BeforeEach with Mockito {

  val shoppingListRepository = mock[ShoppingListRepository]

  val controller = new ShoppingListItemController(shoppingListRepository)

  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  override def before: Any = org.mockito.Mockito.reset(shoppingListRepository)

  "ShoppingListController#save" should {
    "not save item without name" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("quantity", "1"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not save item without quantity" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with negative quantity" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "-1"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with zero quantity" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "0"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with negative price" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "1"), ("priceForOne", "-1.0"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"priceForOne_error")
    }

    "save valid item" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "2"), ("priceForOne", "12.00"))
      val expectedShoppingListItem = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      shoppingListRepository.addItem(Matchers.eq(1), Matchers.eq(expectedShoppingListItem))(any[Session]) returns
        (expectedShoppingListItem.copy(id = Some(2)))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }

  "ShoppingListController#remove" should {
    "remove item from repository" in new WithApplication {
      // when
      controller.remove(1, 1)(FakeRequest())

      // then
      there was one(shoppingListRepository).removeItem(Matchers.eq(1))(any[Session])
    }

    "redirect to shopping list detail" in new WithApplication {
      // when
      val result = controller.remove(1, 1)(FakeRequest())

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("info") must beSome
    }
  }

  "ShoppingListController#edit" should {
    "render editItem template" in new WithApplication {
      // when
      val result = controller.edit(1, 1)(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentAsString(result) must contain("Edit item")
    }
  }

  "ShoppingListController#update" should {
    "not update item without name" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("quantity" -> "2")

      // when
      val result = controller.update(1, 1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not update item without quantity" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "New title")

      // when
      val result = controller.update(1, 1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not update item with negative quantity" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "New title", "quantity" -> "-1")

      // when
      val result = controller.update(1, 1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not update item with negative price for one" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "New title", "quantity" -> "1", "priceForOne" -> "12.00")

      // when
      val result = controller.update(1, 1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beEqualTo("text/html")
      contentAsString(result) must contain("span id=\"priceForOne_error")
    }
  }

}
