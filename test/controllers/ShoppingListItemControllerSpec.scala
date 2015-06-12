package controllers

import models.{ShoppingList, ShoppingListDetail, ShoppingListItem}
import org.mockito.Matchers
import org.specs2.mock.Mockito
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.test.{FakeRequest, PlaySpecification, WithApplicationLoader}
import repositories.{ShoppingListItemRepository, ShoppingListRepository}

import scala.concurrent.Future

/**
 * Created by ferko on 24.3.15.
 */
class ShoppingListItemControllerSpec extends PlaySpecification with Mockito {

  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )
  
  trait WithController extends WithApplicationLoader {
    val shoppingListItemRepository = mock[ShoppingListItemRepository]

    val shoppingListRepository = mock[ShoppingListRepository]
    
    private val app2MessageApi = Application.instanceCache[MessagesApi]

    val controller = new ShoppingListItemController(shoppingListRepository, shoppingListItemRepository, app2MessageApi(app))
  }

  "#save" should {
    "not save item without name" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("quantity", "1"))
      shoppingListRepository.find(Matchers.eq(1)) returns Future.successful(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not save item without quantity" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"))
      shoppingListRepository.find(Matchers.eq(1)) returns Future.successful(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with negative quantity" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "-1"))
      shoppingListRepository.find(Matchers.eq(1)) returns Future.successful(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with zero quantity" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "0"))
      shoppingListRepository.find(Matchers.eq(1)) returns Future.successful(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with negative price" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "1"), ("priceForOne", "-1.0"))
      shoppingListRepository.find(Matchers.eq(1)) returns Future.successful(Some(detail))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"priceForOne_error")
    }

    "save valid item" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "2"), ("priceForOne", "12.00"))
      val expectedShoppingListItem = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      shoppingListItemRepository.add(Matchers.eq(1), Matchers.eq(expectedShoppingListItem)) returns
        Future.successful(expectedShoppingListItem.copy(id = Some(2)))

      // when
      val result = controller.save(1)(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }

  "#remove" should {
    "remove item from repository" in new WithController {
      // when
      controller.remove(1, 1)(FakeRequest())

      // then
      there was one(shoppingListItemRepository).remove(Matchers.eq(1))
    }

    "redirect to shopping list detail" in new WithController {
      // when
      val result = controller.remove(1, 1)(FakeRequest())

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("info") must beSome
    }
  }

  "#edit" should {
    "render editItem template" in new WithController {
      // given
      val item = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      shoppingListItemRepository.find(Matchers.eq(1)) returns Future.successful(Some(item))

      // when
      val result = controller.edit(1, 1)(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Edit item")
    }
  }

  "#update" should {
    "not update item without name" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("quantity" -> "2")

      // when
      val result = controller.update(1, 1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not update item without quantity" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "New title")

      // when
      val result = controller.update(1, 1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not update item with negative quantity" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "New title", "quantity" -> "-1")

      // when
      val result = controller.update(1, 1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not update item with negative price for one" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "New title", "quantity" -> "1", "priceForOne" -> "-12.00")

      // when
      val result = controller.update(1, 1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"priceForOne_error")
    }

    "update existing item" in new WithController {
      // given
      val updated = ShoppingListItem("New title", 1, Some(12.00), Some(2), Some(1))
      val request = FakeRequest().withFormUrlEncodedBody("name" -> updated.name,
        "quantity" -> updated.quantity.toString,
        "priceForOne" -> updated.priceForOne.get.toString)

      // when
      val result = controller.update(1, 2)(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(2).url)
      flash(result).get("info") must beSome[String]

      there was one(shoppingListItemRepository).update(Matchers.eq(updated))
    }
  }

}
