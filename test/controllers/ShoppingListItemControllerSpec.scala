package controllers

import scala.concurrent.Future

import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._

import models.{ShoppingList, ShoppingListDetail, ShoppingListItem}
import repositories.{ShoppingListItemRepository, ShoppingListRepository}

class ShoppingListItemControllerSpec extends HousekeeperControllerSpec {

  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  def withShoppingListRepository[T](f: (ShoppingListRepository, Application) => T) = {
    running((components, app) => f(components.shoppingListRepository, app))
  }

  def withShoppingListItemRepository[T](f: (ShoppingListItemRepository, Application) => T) = {
    running((components, app) => f(components.shoppingListItemRepository, app))
  }

  "#save" should {
    "not save item without name" in withShoppingListRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("quantity", "1"))
      (shoppingListRepository.find _) when (1) returns (Future.successful(Some(detail)))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"name_error")
    }

    "not save item without quantity" in withShoppingListRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("name", "Super title"))
      (shoppingListRepository.find _) when (1) returns (Future.successful(Some(detail)))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not save item with negative quantity" in withShoppingListRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("name", "Super title"), ("quantity", "-1"))
      (shoppingListRepository.find _) when (1) returns (Future.successful(Some(detail)))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not save item with zero quantity" in withShoppingListRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("name", "Super title"), ("quantity", "0"))
      (shoppingListRepository.find _) when (1) returns (Future.successful(Some(detail)))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not save item with negative price" in withShoppingListRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new")
        .withFormUrlEncodedBody(("name", "Super title"), ("quantity", "1"), ("priceForOne", "-1.0"))
      (shoppingListRepository.find _) when (1) returns (Future.successful(Some(detail)))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"priceForOne_error")
    }

    "save valid item" in withShoppingListItemRepository { (shoppingListItemRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/new").withFormUrlEncodedBody(("name", "Super title"), ("quantity", "2"), ("priceForOne", "12.00"))
      val expectedShoppingListItem = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      (shoppingListItemRepository.add _) when (1, expectedShoppingListItem) returns (
        Future.successful(expectedShoppingListItem.copy(id = Some(1))))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some((routes.ShoppingListController.show(1).url)))
      flash(result).get("error") should be(None)
    }
  }

  "#remove" should {
    "remove item from repository" in withShoppingListItemRepository { (shoppingListItemRepository, app) =>
      // given
      (shoppingListItemRepository.remove _) when (1) returns (Future.successful(1))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/items/1"))

      // then
      result.futureValue
      (shoppingListItemRepository.remove _).verify(1)
    }

    "redirect to shopping list detail" in withShoppingListItemRepository { (shoppingListItemRepository, app) =>
      // given
      (shoppingListItemRepository.remove _) when (1) returns (Future.successful(1))
      val request = FakeRequest(GET, "/shopping-lists/1/items/1")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some((routes.ShoppingListController.show(1).url)))
      flash(result).get("info") shouldBe defined
    }
  }

  "#edit" should {
    "render editItem template" in withShoppingListItemRepository { (shoppingListItemRepository, app) =>
      // given
      val item = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      (shoppingListItemRepository.find _) when (1) returns (Future.successful(Some(item)))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/items/1/edit"))

      // then
      status(result) should be(OK)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("Edit item")
    }
  }

  "#update" should {
    "not update item without name" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/1").withFormUrlEncodedBody("quantity" -> "2")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include("span id=\"name_error")
    }

    "not update item without quantity" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/1").withFormUrlEncodedBody("name" -> "New title")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not update item with negative quantity" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/1").withFormUrlEncodedBody("name" -> "New title", "quantity" -> "-1")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not update item with negative price for one" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/items/1").withFormUrlEncodedBody("name" -> "New title", "quantity" -> "1", "priceForOne" -> "-12.00")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"priceForOne_error")
    }

    "update existing item" in withShoppingListItemRepository { (shoppingListItemRepository, app) =>
      // given
      val updated = ShoppingListItem("New title", 1, Some(12.00), Some(2), Some(1))
      val request = FakeRequest(POST, "/shopping-lists/2/items/1").withFormUrlEncodedBody(
        "name" -> updated.name,
        "quantity" -> updated.quantity.toString,
        "priceForOne" -> updated.priceForOne.get.toString
      )
      (shoppingListItemRepository.update _) when (updated) returns (Future.successful(1))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some((routes.ShoppingListController.show(2).url)))
      flash(result).get("info") shouldBe defined

      (shoppingListItemRepository.update _).verify(updated)
    }
  }

}
