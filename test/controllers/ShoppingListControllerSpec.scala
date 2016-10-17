package controllers

import scala.concurrent.Future

import play.api.test.FakeRequest
import play.api.test.Helpers._

import models.{ShoppingList, ShoppingListDetail, ShoppingListItem}
import repositories.ShoppingListRepository
import test.{I18nTestComponents, HousekeeperSpec}

class ShoppingListControllerSpec extends HousekeeperSpec {

  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  val shoppingListRepository = stub[ShoppingListRepository]

  val shoppingListController = new ShoppingListController(
    shoppingListRepository,
    I18nTestComponents.messagesApi)

  "#index" should {
    "render shopping list template" in {
      // given
      (shoppingListRepository.all _) when () returns (Future.successful(List()))

      // when
      val result = shoppingListController.index()(FakeRequest())

      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Shopping Lists")
    }

    "render info message if there aren't any shopping lists" in {
      // given
      (shoppingListRepository.all _) when () returns (Future.successful(List()))

      // when
      val result = shoppingListController.index()(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("No shopping lists were created yet.")
    }

    "render all shopping lists" in {
      // given
      val shoppingLists = List(
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        ShoppingList("Second list", Some("My awesome list"), Some(2)))
      (shoppingListRepository.all _) when () returns (Future.successful(shoppingLists))

      // when
      val result = shoppingListController.index()(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should not include ("No shopping lists were created yet.")
      shoppingLists.foreach { sl =>
        contentAsString(result) should include(sl.title)
      }
    }
  }

  "#show" should {

    "render shopping list detail template" in {
      // given
      (shoppingListRepository.find _) when (1) returns (Future.successful(None))

      // when
      val result = shoppingListController.show(1)(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include ("Shopping List Detail | Housekeeper")
    }

    "render message if shopping detail doesn't exists" in {
      // given
      (shoppingListRepository.find _) when (1) returns (Future.successful(None))

      // when
      val result = shoppingListController.show(1)(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Shopping list was not found.")
    }

    "render shopping list detail" in {
      // given
      val shoppingListDetail = ShoppingListDetail(
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        List(
          ShoppingListItem("Brewdog 5am Saint Red Ale", 1, None, Some(1), Some(1)),
          ShoppingListItem("Macbook Air 13", 1, Some(1000.0), Some(1), Some(2))
        )
      )
      (shoppingListRepository.find _) when (1) returns (Future.successful(Some(shoppingListDetail)))

      // when
      val result = shoppingListController.show(1)(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include(shoppingListDetail.shoppingList.title)
      contentAsString(result) should include(shoppingListDetail.shoppingList.description.get)
      shoppingListDetail.items.foreach { item =>
        contentAsString(result) should include(item.name)
        contentAsString(result) should include(item.quantity.toString)
        contentAsString(result) should include(item.priceForOne.map(_.toString).getOrElse("-"))
      }
    }
  }

  "#newList" should {
    "render newList template" in {
      // when
      val result = shoppingListController.newList()(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("New Shopping List")
    }
  }

  "#save" should {
    "not save shopping list without title" in {
      // given
      val request = FakeRequest()
        .withFormUrlEncodedBody(("description", "testdescription"))

      // when
      val result = shoppingListController.save()(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"title_error")
    }

    "redirect with error message if saving fails" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("title", "Test"), ("description", "Test description"))
      val newShoppingList = ShoppingList("Test", Some("Test description"))
      (shoppingListRepository.save _) when (newShoppingList) returns (Future.successful(newShoppingList))

      // when
      val result = shoppingListController.save()(request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.index().url))
      flash(result).get("error") shouldBe defined
    }

    "save valid shopping list" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "description" -> "testdescription",
        "title" -> "test"
      )
      val expectedShoppingList = ShoppingList("test", Some("testdescription"))
      (shoppingListRepository.save _) when (expectedShoppingList) returns (Future.successful(expectedShoppingList.copy(id = Some(1))))

      // when
      val result = shoppingListController.save()(request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.show(1).url))
      flash(result).get("error") should be(None)
    }
  }

  "#delete" should {
    "remove list from repository" in {
      // when
      (shoppingListRepository.remove _) when (1) returns (Future.successful(1))
      val result = shoppingListController.delete(1)(FakeRequest())
      result.futureValue

      // then
      (shoppingListRepository.remove _).verify(1)
    }

    "redirect to shopping list index" in {
      (shoppingListRepository.remove _) when (1) returns (Future.successful(1))

      // when
      val result = shoppingListController.delete(1)(FakeRequest())

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.index().url))
      flash(result).get("info") shouldBe defined
    }
  }

  "#edit" should {
    "render edit template" in {
      // given
      (shoppingListRepository.find _) when (1) returns (Future.successful(Some(detail)))

      // when
      val result = shoppingListController.edit(1)(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Edit shopping list")
    }

    "redirect to shopping list index if list was not found" in {
      // given
      (shoppingListRepository.find _) when (1) returns (Future.successful(None))

      // when
      val result = shoppingListController.edit(1)(FakeRequest())

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.index().url))
      flash(result).get("error") shouldBe defined
    }
  }

  "#update" should {
    "not update list without title" in {
      // given
      val request = FakeRequest()
        .withFormUrlEncodedBody("description" -> "New test description")

      // when
      val result = shoppingListController.update(1)(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"title_error")
    }

    "update existing list" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "title" -> "New updated title",
        "description" -> "New update description"
      )
      val toUpdate = ShoppingList("New updated title", Option("New update description"), Option(1))
      (shoppingListRepository.update _) when (toUpdate) returns (Future.successful(1))

      // when
      val result = shoppingListController.update(1)(request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.show(1).url))
      flash(result).get("info") shouldBe defined
      (shoppingListRepository.update _).verify(toUpdate)
    }
  }

}
