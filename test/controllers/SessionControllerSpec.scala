package controllers

import scala.concurrent.Future

import org.mindrot.jbcrypt.BCrypt
import play.api.test.FakeRequest
import play.api.test.Helpers._

import models.User
import repositories.UserRepository
import test.{FakeApp, I18nTestComponents, HousekeeperSpec}

class SessionControllerSpec extends HousekeeperSpec {

  val userRepository = stub[UserRepository]
  val sessionController = new SessionController(
    userRepository,
    I18nTestComponents.messagesApi)

  val userA = User(
    "John Doe",
    "doe@example.com",
    BCrypt.hashpw("testPassword", BCrypt.gensalt()))

  "#login" should {

    "render login template" in {
      // when
      val result = sessionController.login()(FakeRequest())

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Please, sign in.")
    }
  }

  "#logout" should {

    "remove username from session" in FakeApp {
      // given
      val request = FakeRequest().withSession(("session.username", "1"))

      // when
      val result = sessionController.logout()(request)

      // then
      session(result).isEmpty should be(true)
    }

    "redirect to home page" in FakeApp {
      // given
      val request = FakeRequest().withSession("session.username" -> "1")

      // when
      val result = sessionController.logout()(request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/"))
    }
  }

  "#authenticate" should {

    "not log in user without email" in {
      // given
      val request = FakeRequest()
        .withFormUrlEncodedBody("password" -> "testPassword")

      // when
      val result = sessionController.authenticate()(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"email_error")
    }

    "not log in user without password" in {
      // given
      val request = FakeRequest()
        .withFormUrlEncodedBody("email" -> "doe@example.com")

      // when
      val result = sessionController.authenticate()(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"password_error")
    }

    "not log in user with invalid password" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "email" -> userA.email,
        "password" -> "totally wrong password")
      (userRepository.findByEmail _) when (userA.email) returns (Future.successful(Some(userA)))

      // when
      val result = sessionController.authenticate()(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Invalid email address or password.")
    }

    "not log in nonexistent user" in {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "email" -> "nonexisting@email.com",
        "password" -> userA.password)
      (userRepository.findByEmail _) when ("nonexisting@email.com") returns (Future.successful(None))

      // when
      val result = sessionController.authenticate()(request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Invalid email address or password.")
    }

    "log in user" in FakeApp {
      // given
      val request = FakeRequest().withFormUrlEncodedBody(
        "email" -> userA.email,
        "password" -> "testPassword")
      (userRepository.findByEmail _) when (userA.email) returns (Future.successful(Some(userA.copy(id = Some(1)))))

      // when
      val result = sessionController.authenticate()(request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.UserController.show(1).url))
      session(result).get("session.username") shouldBe defined
    }
  }
}
