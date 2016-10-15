package controllers

import scala.concurrent.Future

import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._

import models.{ShoppingList, ShoppingListDetail, ShoppingListItem}
import repositories.ShoppingListRepository

class ShoppingListControllerSpec extends HousekeeperControllerSpec {

  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  def withShoppingRepository[T](f: (ShoppingListRepository, Application) => T): T = {
    running((components, app) => f(components.shoppingListRepository, app))
  }

  "#index" should {
    "render shopping list template" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      (shoppingListRepository.all _) when () returns (Future.successful(List()))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists"))

      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Shopping Lists")
    }

    "render info message if there aren't any shopping lists" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      (shoppingListRepository.all _) when () returns (Future.successful(List()))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists"))

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("No shopping lists were created yet.")
    }

    "render all shopping lists" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val shoppingLists = List(
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        ShoppingList("Second list", Some("My awesome list"), Some(2)))
      (shoppingListRepository.all _) when () returns (Future.successful(shoppingLists))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists"))

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

    "render shopping list detail template" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      (shoppingListRepository.find _) when (1) returns (Future.successful(None))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1"))

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include ("Shopping List Detail | Housekeeper")
    }

    "render message if shopping detail doesn't exists" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      (shoppingListRepository.find _) when (1) returns (Future.successful(None))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1"))

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Shopping list was not found.")
    }

    "render shopping list detail" in withShoppingRepository { (shoppingListRepository, app) =>
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
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1"))

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
    "render newList template" in withShoppingRepository { (shoppingListRepository, app) =>
      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/new"))

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("New Shopping List")
    }
  }

  "#save" should {
    "not save shopping list without title" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists").withFormUrlEncodedBody(("description", "testdescription"))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"title_error")
    }

    "redirect with error message if saving fails" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists").withFormUrlEncodedBody(("title", "Test"), ("description", "Test description"))
      val newShoppingList = ShoppingList("Test", Some("Test description"))
      (shoppingListRepository.save _) when (newShoppingList) returns (Future.successful(newShoppingList))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.index().url))
      flash(result).get("error") shouldBe defined
    }

    "save valid shopping list" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists").withFormUrlEncodedBody(
        "description" -> "testdescription",
        "title" -> "test"
      )
      val expectedShoppingList = ShoppingList("test", Some("testdescription"))
      (shoppingListRepository.save _) when (expectedShoppingList) returns (Future.successful(expectedShoppingList.copy(id = Some(1))))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.show(1).url))
      flash(result).get("error") should be(None)
    }
  }

  "#delete" should {
    "remove list from repository" in withShoppingRepository { (shoppingListRepository, app) =>
      // when
      (shoppingListRepository.remove _) when (1) returns (Future.successful(1))
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/delete"))
      result.futureValue

      // then
      (shoppingListRepository.remove _).verify(1)
    }

    "redirect to shopping list index" in withShoppingRepository { (shoppingListRepository, app) =>
      (shoppingListRepository.remove _) when (1) returns (Future.successful(1))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/delete"))

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.index().url))
      flash(result).get("info") shouldBe defined
    }
  }

  "#edit" should {
    "render edit template" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      (shoppingListRepository.find _) when (1) returns (Future.successful(Some(detail)))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/edit"))

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Edit shopping list")
    }

    "redirect to shopping list index if list was not found" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      (shoppingListRepository.find _) when (1) returns (Future.successful(None))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/edit"))

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.index().url))
      flash(result).get("error") shouldBe defined
    }
  }

  "#update" should {
    "not update list without title" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/edit").withFormUrlEncodedBody("description" -> "New test description")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"title_error")
    }

    "update existing list" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/edit").withFormUrlEncodedBody(
        "title" -> "New updated title",
        "description" -> "New update description"
      )
      val toUpdate = ShoppingList("New updated title", Option("New update description"), Option(1))
      (shoppingListRepository.update _) when (toUpdate) returns (Future.successful(1))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ShoppingListController.show(1).url))
      flash(result).get("info") shouldBe defined
      (shoppingListRepository.update _).verify(toUpdate)
    }
  }

}
