package controllers

import models.{ShoppingList, ShoppingListDetail, ShoppingListItem}
import org.specs2.execute.{AsResult, Result}
import org.specs2.mock.Mockito
import play.api.Application
import play.api.test.{FakeRequest, PlaySpecification}
import repositories.ShoppingListRepository

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ShoppingListControllerSpec extends PlaySpecification with Mockito {

  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  def withShoppingRepository[T: AsResult](f: (ShoppingListRepository, Application) => T): Result = {
    WithControllers.running((components, app) => f(components.shoppingListRepository, app))
  }

  "#index" should {
    "render shopping list template" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      shoppingListRepository.all returns Future.successful(List())

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Shopping Lists")
    }

    "render info message if there aren't any shopping lists" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      shoppingListRepository.all returns Future.successful(List())

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists"))

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("No shopping lists were created yet.")
    }

    "render all shopping lists" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val shoppingLists = List(
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        ShoppingList("Second list", Some("My awesome list"), Some(2)))
      shoppingListRepository.all returns Future.successful(shoppingLists)

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists"))

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must not contain ("No shopping lists were created yet.")
      shoppingLists.foreach { sl =>
        contentAsString(result) must contain(sl.title)
      }
    }
  }

  "#show" should {

    "render shopping list detail template" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      shoppingListRepository.find(1) returns Future.successful(None)

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1"))

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping List Detail | Housekeeper")
    }

    "render message if shopping detail doesn't exists" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      shoppingListRepository.find(1) returns Future.successful(None)

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1"))

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping list was not found.")
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
      shoppingListRepository.find(1) returns Future.successful(Some(shoppingListDetail))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1"))

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

  "#newList" should {
    "render newList template" in withShoppingRepository { (shoppingListRepository, app) =>
      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/new"))

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("New Shopping List")
    }
  }

  "#save" should {
    "not save shopping list without title" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists").withFormUrlEncodedBody(("description", "testdescription"))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"title_error")
    }

    "redirect with error message if saving fails" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists").withFormUrlEncodedBody(("title", "Test"), ("description", "Test description"))
      val newShoppingList = ShoppingList("Test", Some("Test description"))
      shoppingListRepository.save(newShoppingList) returns Future.successful(newShoppingList)

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("error") must beSome
    }

    "save valid shopping list" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists").withFormUrlEncodedBody(
        "description" -> "testdescription",
        "title" -> "test"
      )
      val expectedShoppingList = ShoppingList("test", Some("testdescription"))
      shoppingListRepository.save(expectedShoppingList) returns Future.successful(expectedShoppingList.copy(id = Some(1)))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }

  "#delete" should {
    "remove list from repository" in withShoppingRepository { (shoppingListRepository, app) =>
      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/delete"))
      Await.ready(result, 1.second)

      // then
      there was one(shoppingListRepository).remove(1)
    }

    "redirect to shopping list index" in withShoppingRepository { (shoppingListRepository, app) =>
      shoppingListRepository.remove(1) returns Future.successful(1)

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/delete"))

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("info") must beSome
    }
  }

  "#edit" should {
    "render edit template" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      shoppingListRepository.find(1) returns Future.successful(Some(detail))

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/edit"))

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Edit shopping list")
    }

    "redirect to shopping list index if list was not found" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      shoppingListRepository.find(1) returns Future.successful(None)

      // when
      val Some(result) = route(app, FakeRequest(GET, "/shopping-lists/1/edit"))

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("error") must beSome
    }
  }

  "#update" should {
    "not update list without title" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/edit").withFormUrlEncodedBody("description" -> "New test description")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"title_error")
    }

    "update existing list" in withShoppingRepository { (shoppingListRepository, app) =>
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/edit").withFormUrlEncodedBody(
        "title" -> "New updated title",
        "description" -> "New update description"
      )
      val toUpdate = ShoppingList("New updated title", Option("New update description"), Option(1))
      shoppingListRepository.update(toUpdate) returns Future.successful(1)

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("info") must beSome
      there was one(shoppingListRepository).update(toUpdate)
    }
  }

}
