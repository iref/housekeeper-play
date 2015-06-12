package controllers

import global.HousekeeperComponent
import models.{ShoppingList, ShoppingListDetail, ShoppingListItem}
import org.specs2.mock.Mockito
import play.api.ApplicationLoader.Context
import play.api.test.{FakeRequest, PlaySpecification, WithApplicationLoader}
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import repositories.{ShoppingListItemRepository, ShoppingListRepository}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
 * Created by ferko on 24.3.15.
 */
class ShoppingListItemControllerSpec extends PlaySpecification with Mockito {

  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  val shoppingListRepositoryMock = mock[ShoppingListRepository]
  val shoppingListItemRepositoryMock = mock[ShoppingListItemRepository]
  
  trait ShoppingListItemControllerTestComponents extends HousekeeperComponent {
    override lazy val shoppingListRepository = shoppingListRepositoryMock
    override lazy val shoppingListItemRepository = shoppingListItemRepositoryMock
  }

  class ShoppingListItemControllerTestLoader extends ApplicationLoader {
    override def load(context: Context): Application = {
      (new BuiltInComponentsFromContext(context) with ShoppingListItemControllerTestComponents).application
    }
  }

  class WithController extends WithApplicationLoader(applicationLoader = new ShoppingListItemControllerTestLoader)

  "#save" should {
    "not save item without name" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("quantity", "1"))
      shoppingListRepositoryMock.find(1) returns Future.successful(Some(detail))

      // when
      val Some(result) = route(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not save item without quantity" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("name", "Super title"))
      shoppingListRepositoryMock.find(1) returns Future.successful(Some(detail))

      // when
      val Some(result) = route(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with negative quantity" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("name", "Super title"), ("quantity", "-1"))
      shoppingListRepositoryMock.find(1) returns Future.successful(Some(detail))

      // when
      val Some(result) = route(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with zero quantity" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("name", "Super title"), ("quantity", "0"))
      shoppingListRepositoryMock.find(1) returns Future.successful(Some(detail))

      // when
      val Some(result) = route(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with negative price" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new")
        .withFormUrlEncodedBody(("name", "Super title"), ("quantity", "1"), ("priceForOne", "-1.0"))
      shoppingListRepositoryMock.find(1) returns Future.successful(Some(detail))

      // when
      val Some(result) = route(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"priceForOne_error")
    }

    "save valid item" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("name", "Super title"), ("quantity", "2"), ("priceForOne", "12.00"))
      val expectedShoppingListItem = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      shoppingListItemRepositoryMock.add(1, expectedShoppingListItem) returns
        Future.successful(expectedShoppingListItem.copy(id = Some(2)))

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }

  "#remove" should {
    "remove item from repository" in new WithController {
      // given
      shoppingListItemRepositoryMock.remove(1) returns Future.successful(1)

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1/items/1"))

      // then
      Await.ready(result, 1.second)
      there was one(shoppingListItemRepositoryMock).remove(1)
    }

    "redirect to shopping list detail" in new WithController {
      // given
      shoppingListItemRepositoryMock.remove(1) returns Future.successful(1)
      val request = FakeRequest(GET, "/shopping-lists/1/items/1")

      // when
      val Some(result) = route(request)

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
      shoppingListItemRepositoryMock.find(1) returns Future.successful(Some(item))

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1/items/1/edit"))

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Edit item")
    }
  }

  "#update" should {
    "not update item without name" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/1").withFormUrlEncodedBody("quantity" -> "2")

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not update item without quantity" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/1").withFormUrlEncodedBody("name" -> "New title")

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not update item with negative quantity" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/1").withFormUrlEncodedBody("name" -> "New title", "quantity" -> "-1")

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not update item with negative price for one" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/1").withFormUrlEncodedBody("name" -> "New title", "quantity" -> "1", "priceForOne" -> "-12.00")

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"priceForOne_error")
    }

    "update existing item" in new WithController {
      // given
      val updated = ShoppingListItem("New title", 1, Some(12.00), Some(2), Some(1))
      val request = FakeRequest(POST, "/shopping-lists/2/items/1").withFormUrlEncodedBody(
        "name" -> updated.name,
        "quantity" -> updated.quantity.toString,
        "priceForOne" -> updated.priceForOne.get.toString
      )
      shoppingListItemRepositoryMock.update(updated) returns(Future.successful(1))

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(2).url)
      flash(result).get("info") must beSome[String]

      there was one(shoppingListItemRepositoryMock).update(updated)
    }
  }

}
