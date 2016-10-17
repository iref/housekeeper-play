package controllers

import scala.concurrent.Future

import play.api.test.FakeRequest
import play.api.test.Helpers._

import models.{ShoppingList, ShoppingListDetail, ShoppingListItem}
import repositories.{ShoppingListItemRepository, ShoppingListRepository}
import test.{I18nTestComponents, HousekeeperSpec}

class ShoppingListItemControllerSpec extends HousekeeperSpec {

  trait Fixtures {
    val detail = ShoppingListDetail(
      ShoppingList("Test list", Some("Test description"), Some(1)),
      List()
    )

    val shoppingListRepository = stub[ShoppingListRepository]
    val shoppingListItemRepository = stub[ShoppingListItemRepository]

    (shoppingListRepository.find _) when (1) returns (Future.successful(Some(detail)))

    lazy val shoppingListItemController = new ShoppingListItemController(
      shoppingListRepository,
      shoppingListItemRepository,
      I18nTestComponents.messagesApi
    )
  }

  "#save" should {
    "not save item without name" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("quantity" -> "1")

      // when
      val result = shoppingListItemController.save(1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"name_error")
    }

    "not save item without quantity" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "Super title")

      // when
      val result = shoppingListItemController.save(1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not save item with negative quantity" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> "Super title",
        "quantity" -> "-1")

      // when
      val result = shoppingListItemController.save(1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not save item with zero quantity" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> "Super title",
        "quantity" -> "0")

      // when
      val result = shoppingListItemController.save(1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not save item with negative price" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> "Super title",
        "quantity" -> "1",
        "priceForOne" -> "-1.0")

      // when
      val result = shoppingListItemController.save(1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"priceForOne_error")
    }

    "save valid item" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> "Super title",
        "quantity" -> "2",
        "priceForOne" -> "12.00")
      val expectedShoppingListItem = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      (shoppingListItemRepository.add _) when (1, expectedShoppingListItem) returns (
        Future.successful(expectedShoppingListItem.copy(id = Some(1))))

      // when
      val result = shoppingListItemController.save(1)(request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some((routes.ShoppingListController.show(1).url)))
      flash(result).get("error") should be(None)
    }
  }

  "#remove" should {
    "remove item from repository" in new Fixtures {
      // given
      (shoppingListItemRepository.remove _) when (1) returns (Future.successful(1))

      // when
      val result = shoppingListItemController.remove(1, 1)(FakeRequest())

      // then
      result.futureValue
      (shoppingListItemRepository.remove _).verify(1)
    }

    "redirect to shopping list detail" in new Fixtures {
      // given
      (shoppingListItemRepository.remove _) when (1) returns (Future.successful(1))
      val request = FakeRequest()

      // when
      val result = shoppingListItemController.remove(1, 1)(request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some((routes.ShoppingListController.show(1).url)))
      flash(result).get("info") shouldBe defined
    }
  }

  "#edit" should {
    "render editItem template" in new Fixtures {
      // given
      val item = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      (shoppingListItemRepository.find _) when (1) returns (Future.successful(Some(item)))

      // when
      val result = shoppingListItemController.edit(1, 1)(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("Edit item")
    }
  }

  "#update" should {
    "not update item without name" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("quantity" -> "2")

      // when
      val result = shoppingListItemController.update(1, 1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include("span id=\"name_error")
    }

    "not update item without quantity" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "New title")

      // when
      val result = shoppingListItemController.update(1, 1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not update item with negative quantity" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> "New title",
        "quantity" -> "-1")

      // when
      val result = shoppingListItemController.update(1, 1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include("span id=\"quantity_error")
    }

    "not update item with negative price for one" in new Fixtures {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> "New title",
        "quantity" -> "1",
        "priceForOne" -> "-12.00")

      // when
      val result = shoppingListItemController.update(1, 1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some(("text/html")))
      contentAsString(result) should include("span id=\"priceForOne_error")
    }

    "update existing item" in new Fixtures {
      // given
      val updated = ShoppingListItem("New title", 1, Some(12.00), Some(2), Some(1))
      val request = FakeRequest().withFormUrlEncodedBody(
        "name" -> updated.name,
        "quantity" -> updated.quantity.toString,
        "priceForOne" -> updated.priceForOne.get.toString
      )
      (shoppingListItemRepository.update _) when (updated) returns (Future.successful(1))

      // when
      val result = shoppingListItemController.update(1, 2)(request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some((routes.ShoppingListController.show(2).url)))
      flash(result).get("info") shouldBe defined

      (shoppingListItemRepository.update _).verify(updated)
    }
  }

}
