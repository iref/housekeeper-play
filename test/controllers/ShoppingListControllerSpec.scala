package controllers

import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.test._
import global.HousekeeperComponent
import models.{User, ShoppingListItem, ShoppingList, ShoppingListDetail}
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mock.Mockito
import play.api.ApplicationLoader.Context
import play.api.{BuiltInComponentsFromContext, ApplicationLoader, Application}
import play.api.test.{FakeRequest, PlaySpecification, WithApplicationLoader}
import repositories.ShoppingListRepository

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ShoppingListControllerSpec extends PlaySpecification with Mockito {

  val shoppingListRepositoryMock = mock[ShoppingListRepository]

  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  val user = User("John Doe", "doe@test.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()), Option(1))

  implicit val authEnv = FakeEnvironment[User, CookieAuthenticator](Seq(user.loginInfo -> user))

  class WithController extends WithApplicationLoader(applicationLoader = new ShoppingListControllerTestLoader)

  trait ShoppingListControllerTestComponents extends HousekeeperComponent {
    override lazy val shoppingListRepository = shoppingListRepositoryMock
    override lazy val env = authEnv
  }

  class ShoppingListControllerTestLoader extends ApplicationLoader {
    override def load(context: Context): Application = {
      (new BuiltInComponentsFromContext(context) with ShoppingListControllerTestComponents).application
    }
  }

  "#index" should {
    "render shopping list template" in new WithController {
      // given
      shoppingListRepositoryMock.all returns Future.successful(List())

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists").withAuthenticator(user.loginInfo))

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Shopping Lists")
    }

    "render info message if there aren't any shopping lists" in new WithController {
      // given
      shoppingListRepositoryMock.all returns Future.successful(List())

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists").withAuthenticator(user.loginInfo))

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
      shoppingListRepositoryMock.all returns Future.successful(shoppingLists)

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists").withAuthenticator(user.loginInfo))

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
      shoppingListRepositoryMock.find(1) returns Future.successful(None)

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1").withAuthenticator(user.loginInfo))

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping List Detail | Housekeeper")
    }

    "render message if shopping detail doesn't exists" in new WithController {
      // given
      shoppingListRepositoryMock.find(1) returns Future.successful(None)

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1").withAuthenticator(user.loginInfo))

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
      shoppingListRepositoryMock.find(1) returns Future.successful(Some(shoppingListDetail))

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1").withAuthenticator(user.loginInfo))

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
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/new").withAuthenticator(user.loginInfo))

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("New Shopping List")
    }
  }

  "#save" should {
    "not save shopping list without title" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists")
        .withFormUrlEncodedBody(("description", "testdescription"))
        .withAuthenticator(user.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"title_error")
    }

    "redirect with error message if saving fails" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists")
        .withFormUrlEncodedBody(("title", "Test"), ("description", "Test description"))
        .withAuthenticator(user.loginInfo)
      val newShoppingList = ShoppingList("Test", Some("Test description"))
      shoppingListRepositoryMock.save(newShoppingList) returns Future.successful(newShoppingList)

      // when
      val Some(result) = route(request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("error") must beSome
    }

    "save valid shopping list" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists").withFormUrlEncodedBody(
        "description" -> "testdescription",
        "title" -> "test"
      ).withAuthenticator(user.loginInfo)
      val expectedShoppingList = ShoppingList("test", Some("testdescription"))
      shoppingListRepositoryMock.save(expectedShoppingList) returns Future.successful(expectedShoppingList.copy(id = Some(1)))

      // when
      val Some(result) = route(request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }

  "#delete" should {
    "remove list from repository" in new WithController {
      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1/delete").withAuthenticator(user.loginInfo))
      Await.ready(result, 1.second)

      // then
      there was one(shoppingListRepositoryMock).remove(1)
    }

    "redirect to shopping list index" in new WithController {
      shoppingListRepositoryMock.remove(1) returns Future.successful(1)

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1/delete").withAuthenticator(user.loginInfo))

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("info") must beSome
    }
  }

  "#edit" should {
    "render edit template" in new WithController {
      // given
      shoppingListRepositoryMock.find(1) returns Future.successful(Some(detail))

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1/edit").withAuthenticator(user.loginInfo))

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Edit shopping list")
    }

    "redirect to shopping list index if list was not found" in new WithController {
      // given
      shoppingListRepositoryMock.find(1) returns Future.successful(None)

      // when
      val Some(result) = route(FakeRequest(GET, "/shopping-lists/1/edit").withAuthenticator(user.loginInfo))

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("error") must beSome
    }
  }

  "#update" should {
    "not update list without title" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/edit")
        .withFormUrlEncodedBody("description" -> "New test description")
        .withAuthenticator(user.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"title_error")
    }

    "update existing list" in new WithController {
      // given
      val request = FakeRequest(POST, "/shopping-lists/1/edit").withFormUrlEncodedBody(
        "title" -> "New updated title",
        "description" -> "New update description"
      ).withAuthenticator(user.loginInfo)
      val toUpdate = ShoppingList("New updated title", Option("New update description"), Option(1))
      shoppingListRepositoryMock.update(toUpdate) returns Future.successful(1)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("info") must beSome
      there was one(shoppingListRepositoryMock).update(toUpdate)
    }
  }

}
