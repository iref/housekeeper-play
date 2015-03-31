package controllers

import models.{User, UserRepository}
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.test.{FakeRequest, WithApplication, PlaySpecification}

class UserControllerSpec extends PlaySpecification with BeforeEach with Mockito {

  val userRepository = mock[UserRepository]

  val userA = User("John Doe", "doe@example.com", "testPassword")

  val controller = new UserController(userRepository)

  def before = org.mockito.Mockito.reset(userRepository)

  "#register" should {

    "render new user template" in new WithApplication {
      // when
      val result = controller.register()(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Create new account")
    }
  }

  "#create" should {

    "not create user without name" in new WithApplication {
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

    "not create user without email" in new WithApplication {
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

    "not create user without password" in new WithApplication {
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

    "not create user without passwordConfirmation" in new WithApplication {
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

    "not create user with password shorter than 6 characters" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name, "email" -> userA.email,
        "password" -> "abcde", "passwordConfirmation" -> "abcde")
    }

    "not create user if password does not match confirmation" in new WithApplication {
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

    "redirect to user detail after successful user creation" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("name" -> userA.name, "email" -> userA.email,
        "password" -> userA.password, "passwordConfirmation" -> userA.password)

      // when
      val result = controller.save()(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.Application.index().url)
      flash(result).get("info") must beSome
    }
  }

}
