package controllers

import models.{ShoppingListItem, ShoppingList, ShoppingListDetail}
import org.mockito.Matchers
import org.specs2.mock.Mockito
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.test.{FakeRequest, PlaySpecification, WithApplicationLoader}
import repositories.ShoppingListRepository

import scala.concurrent.Future

class ShoppingListControllerSpec extends PlaySpecification with Mockito {


  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  trait WithController extends WithApplicationLoader {
    private val app2MessagesApi = Application.instanceCache[MessagesApi]

    val shoppingListRepository = mock[ShoppingListRepository]
    val controller = new ShoppingListController(shoppingListRepository, app2MessagesApi(app))
  }

  "#index" should {
    "render shopping list template" in new WithController {
      // given
      shoppingListRepository.all returns Future.successful(List())

      // when
      val result = controller.index()(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Shopping Lists")
    }

    "render info message if there aren't any shopping lists" in new WithController {
      // given
      shoppingListRepository.all returns Future.successful(List())

      // when
      val result = controller.index()(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("No shopping lists were created yet.")
    }

    "render all shopping lists" in new WithController {
      // given
      val shoppingLists = List(
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        ShoppingList("Second list", Some("My awesome list"), Some(2)))
      shoppingListRepository.all returns Future.successful(shoppingLists)

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

  "#show" should {

    "render shopping list detail template" in new WithController {
      // given
      shoppingListRepository.find(any[Int]) returns Future.successful(None)

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping List Detail | Housekeeper")
    }

    "render message if shopping detail doesn't exists" in new WithController {
      // given
      shoppingListRepository.find(any[Int]) returns Future.successful(None)

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping list was not found.")
    }

    "render shopping list detail" in new WithController {
      // given
      val shoppingListDetail = ShoppingListDetail(
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        List(
          ShoppingListItem("Brewdog 5am Saint Red Ale", 1, None, Some(1), Some(1)),
          ShoppingListItem("Macbook Air 13", 1, Some(1000.0), Some(1), Some(2))
        )
      )
      shoppingListRepository.find(Matchers.eq(1)) returns Future.successful(Some(shoppingListDetail))

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

  "#newList" should {
    "render newList template" in new WithController {
      // when
      val result = controller.newList()(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("New Shopping List")
    }
  }

  "#save" should {
    "not save shopping list without title" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("description", "testdescription"))

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"title_error")
    }

    "redirect with error message if saving fails" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("title", "Test"), ("description", "Test description"))
      val newShoppingList = ShoppingList("Test", Some("Test description"))
      shoppingListRepository.save(Matchers.eq(newShoppingList)) returns Future.successful(newShoppingList)

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("error") must beSome
    }

    "save valid shopping list" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("description", "testdescription"), ("title", "test"))
      val expectedShoppingList = ShoppingList("test", Some("testdescription"))
      shoppingListRepository.save(Matchers.eq(expectedShoppingList)) returns Future.successful(expectedShoppingList.copy(id = Some(1)))

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }

  "#delete" should {
    "remove list from repository" in new WithController {
      // when
      controller.delete(1)(FakeRequest())

      // then
      there was one(shoppingListRepository).remove(Matchers.eq(1))
    }

    "redirect to shopping list index" in new WithController {
      // when
      val result = controller.delete(1)(FakeRequest())

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("info") must beSome
    }
  }

  "#edit" should {
    "render edit template" in new WithController {
      // given
      shoppingListRepository.find(Matchers.eq(1)) returns Future.successful(Some(detail))

      // when
      val result = controller.edit(1)(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Edit shopping list")
    }

    "redirect to shopping list index if list was not found" in new WithController {
      // given
      shoppingListRepository.find(Matchers.eq(1)) returns Future.successful(None)

      // when
      val result = controller.edit(1)(FakeRequest())

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("error") must beSome
    }
  }

  "#update" should {
    "not update list without title" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("description" -> "New test description")

      // when
      val result = controller.update(1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"title_error")
    }

    "update existing list" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("title" -> "New updated title",
        "description" -> "New update description")

      // when
      val result = controller.update(1)(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("info") must beSome
    }
  }

}
