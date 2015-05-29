package controllers

import models.{User, UserRepositoryImpl}
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Matchers
import org.specs2.mock.Mockito
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.test.{FakeRequest, PlaySpecification, WithApplicationLoader}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserControllerSpec extends PlaySpecification with Mockito {

  val userA = User("John Doe", "doe@example.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()))

  trait WithController extends WithApplicationLoader {
    val userRepository = mock[UserRepositoryImpl]

    private val app2MessageApi = Application.instanceCache[MessagesApi]

    val controller = new UserController(userRepository, app2MessageApi(app))
  }

  "#register" should {

    "render new user template" in new WithController {
      // when
      val result = controller.register()(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Create new account")
    }
  }

  "#create" should {

    "not create user without name" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> userA.email,
        "password" -> userA.password, "passwordConfirmation" -> userA.password)

      // when
      val result = controller.save()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not create user without email" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name,
        "password" -> userA.password, "passwordConfirmation" -> userA.password)

      // when
      val result = controller.save()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"email_error")
    }

    "not create user without password" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name, "email" -> userA.email,
        "passwordConfirmation" -> userA.password)

      // when
      val result = controller.save()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"password_error")
    }

    "not create user without passwordConfirmation" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name, "email" -> userA.email,
        "password" -> userA.password)

      // when
      val result = controller.save()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"passwordConfirmation_error")
    }

    "not create user with password shorter than 6 characters" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name, "email" -> userA.email,
        "password" -> "abcde", "passwordConfirmation" -> "abcde")
    }

    "not create user if password does not match confirmation" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name, "email" -> userA.email,
        "password" -> userA.password, "passwordConfirmation" -> (userA.password + "XXX"))

      // when
      val result = controller.save()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("div class=\"alert alert-danger")
    }

    "redirect to user detail after successful user creation" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name, "email" -> userA.email,
        "password" -> userA.password, "passwordConfirmation" -> userA.password)
      userRepository.save(any[User]) returns Future.successful(userA.copy(id = Some(2)))

      // when
      val result = controller.save()(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(2).url)
      flash(result).get("info") must beSome
      session(result).get("session.username") must beSome
    }
  }

  "#show" should {

    "render user detail template" in new WithController {
      // given
      userRepository.find(Matchers.eq(1)) returns Future.successful(Some(userA.copy(id = Some(1))))

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("User profile")
      contentAsString(result) must contain(userA.name)
    }

    "redirect to index if user does not exists" in new WithController {
      // given
      userRepository.find(Matchers.eq(1)) returns Future.successful(None)

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ApplicationController.index().url)
      flash(result).get("error") must beSome
    }
  }

  "#edit" should {
    "render edit user template for existing user" in new WithController {
      // given
      userRepository.find(Matchers.eq(1)) returns Future.successful(Some(userA.copy(id = Some(1))))

      // when
      val result = controller.edit(1)(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Edit profile")
    }

    "redirect to index if user with does not exist" in new WithController {
      // given
      userRepository.find(Matchers.eq(1)) returns Future.successful(None)

      // when
      val result = controller.edit(1)(FakeRequest())

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ApplicationController.index().url)
      flash(result).get("error") must beSome
    }
  }

  "#update" should {

    "not update user without name" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> "test@example.com")

      // when
      val result = controller.update(1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not update user without email" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "test")

      // when
      val result = controller.update(1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"email_error")
    }

    "not update user password if confirmation is missing" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "test", "email" -> "test@example.com",
        "password" -> "testPassword2", "oldPassword" -> "testPassword")

      // when
      val result = controller.update(1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("div class=\"alert alert-danger")
    }

    "not update user password if it does not match confirmation" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name, "email" -> userA.email,
        "password" -> "testPasswordXXX", "passwordConfirmation" -> userA.password)

      // when
      val result = controller.update(1)(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("div class=\"alert alert-danger")
    }

    "update user in repository" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "Updated user name", "email" -> "updated@example.com",
        "password" -> "newUpdatedPasswordXXX", "passwordConfirmation" -> "newUpdatedPasswordXXX")

      // when
      val result = controller.update(1)(request)

      // then
      there was one(userRepository).update(any[User])
    }

    "update user if password is not provided" in new WithController {
      // given
      val expectedUser = userA.copy(name = "Passwordless user update", email = "updated@example.com", id = Some(1))
      val request = FakeRequest().withFormUrlEncodedBody("name" -> expectedUser.name, "email" -> expectedUser.email)
      userRepository.find(Matchers.eq(1)) returns Future.successful(Some(userA.copy(id = Some(1))))

      // when
      val result = controller.update(1)(request)

      // then
      there was one(userRepository).update(any[User])
    }

    "redirect to user detail after successful update" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> "Updated user name", "email" -> "updated@example.com",
        "password" -> "newPassword", "passwordConfirmation" -> "newPassword")

      // when
      val result = controller.update(1)(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(1).url)
    }

  }

}
