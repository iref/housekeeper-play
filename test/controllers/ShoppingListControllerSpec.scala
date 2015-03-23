package controllers

import models.{ShoppingListItem, ShoppingListDetail, ShoppingList, ShoppingListRepository}
import org.mockito.Matchers
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.db.DB
import play.api.db.slick.Config.driver.simple._
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

class ShoppingListControllerSpec extends PlaySpecification with BeforeEach with Mockito {

  val shoppingListRepository = mock[ShoppingListRepository]
  val controller = new ShoppingListController(shoppingListRepository)
  val detail = ShoppingListDetail(
    ShoppingList("Test list", Some("Test description"), Some(1)),
    List()
  )

  override def before: Any = org.mockito.Mockito.reset(shoppingListRepository)

  "ShoppingListController#index" should {
    "render shopping list template" in new WithApplication {
      // given
      shoppingListRepository.all(any[Session]) returns List()

      // when
      val result = controller.index()(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Shopping Lists")
    }

    "render info message if there aren't any shopping lists" in new WithApplication {
      // given
      shoppingListRepository.all(any[Session]) returns List()

      // when
      val result = controller.index()(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("No shopping lists were created yet.")
    }

    "render all shopping lists" in new WithApplication {
      // given
      val shoppingLists = List(
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        ShoppingList("Second list", Some("My awesome list"), Some(2)))
      shoppingListRepository.all(any[Session]) returns shoppingLists

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

  "ShoppingListController#show" should {

    "render shopping list detail template" in new WithApplication {
      // given
      shoppingListRepository.find(any[Int])(any[Session]) returns(None)

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping List Detail | Housekeeper")
    }

    "render message if shopping detail doesn't exists" in new WithApplication {
      // given
      shoppingListRepository.find(any[Int])(any[Session]) returns(None)

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain ("Shopping list was not found.")
    }

    "render shopping list detail" in new WithApplication {
      // given
      val shoppingListDetail = ShoppingListDetail(
        ShoppingList("First list", Some("Newbie list"), Some(1)),
        List(
          ShoppingListItem("Brewdog 5am Saint Red Ale", 1, None, Some(1), Some(1)),
          ShoppingListItem("Macbook Air 13", 1, Some(1000.0), Some(1), Some(2))
        )
      )
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns Some(shoppingListDetail)

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

  "ShoppingListController#new" should {
    "render edit template" in {
      // when
      val result = controller.newList()(FakeRequest())

      // then
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("New Shopping List")
    }
  }

  "ShoppingListController#save" should {
    "not save shopping list without title" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("description", "testdescription"))

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"title_error")
    }

    "redirect with error message if saving fails" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("title", "Test"), ("description", "Test description"))
      val newShoppingList = ShoppingList("Test", Some("Test description"))
      shoppingListRepository.save(Matchers.eq(newShoppingList))(any[Session]) returns(newShoppingList)

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.index().url)
      flash(result).get("error") must beSome
    }

    "save valid shopping list" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("description", "testdescription"), ("title", "test"))
      val expectedShoppingList = ShoppingList("test", Some("testdescription"))
      shoppingListRepository.save(Matchers.eq(expectedShoppingList))(any[Session]) returns(expectedShoppingList.copy(id = Some(1)))

      // when
      val result = controller.save()(request)

      // then
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }

  "ShoppingListController#addItem" should {
    "not save item without name" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("quantity", "1"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.addItem(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not save item without quantity" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.addItem(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with negative quantity" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "-1"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.addItem(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with zero quantity" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "0"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.addItem(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"quantity_error")
    }

    "not save item with negative price" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "1"), ("priceForOne", "-1.0"))
      shoppingListRepository.find(Matchers.eq(1))(any[Session]) returns(Some(detail))

      // when
      val result = controller.addItem(1)(request)

      // then
      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"priceForOne_error")
    }

    "save valid item" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(("name", "Super title"), ("quantity", "2"), ("priceForOne", "12.00"))
      val expectedShoppingListItem = ShoppingListItem("Super title", 2, Some(12.00), Some(1))
      shoppingListRepository.addItem(Matchers.eq(1), Matchers.eq(expectedShoppingListItem))(any[Session]) returns
        (expectedShoppingListItem.copy(id = Some(2)))

      // when
      val result = controller.addItem(1)(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("error") must beNone
    }
  }

  "ShoppingListController#removeItem" should {
    "remove item from repository" in {
      // when
      controller.removeItem(1, 1)(FakeRequest())

      // then
      there was one(shoppingListRepository).removeItem(Matchers.eq(1))(any[Session])
    }

    "redirect to shopping list detail" in {
      // when
      val result = controller.removeItem(1, 1)(FakeRequest())

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ShoppingListController.show(1).url)
      flash(result).get("info") must beSome
    }
  }
}
