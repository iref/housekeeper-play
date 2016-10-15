package controllers

import scala.concurrent.Future

import org.mindrot.jbcrypt.BCrypt
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._

import models.User
import repositories.UserRepository

class SessionControllerSpec extends HousekeeperControllerSpec {

  val userA = User(
    "John Doe",
    "doe@example.com",
    BCrypt.hashpw("testPassword", BCrypt.gensalt()))

  def withUserRepository[T](f: (UserRepository, Application) => T): T = {
    running((components, app) => f(components.userRepository, app))
  }

  "#login" should {

    "render login template" in running { (_, app) =>
      // when
      val Some(result) = route(app, FakeRequest(GET, "/login"))

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Please, sign in.")
    }
  }

  "#logout" should {

    "remove username from session" in running { (components, app) =>
      // given
      val request = FakeRequest(GET, "/logout").withSession(("session.username", "1"))

      // when
      val Some(result) = route(app, request)

      // then
      session(result).isEmpty should be(true)
    }

    "redirect to home page" in running { (_, app) =>
      // given
      val request = FakeRequest(GET, "/logout").withSession("session.username" -> "1")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/"))
    }
  }

  "#authenticate" should {

    "not log in user without email" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/login")
        .withFormUrlEncodedBody("password" -> "testPassword")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"email_error")
    }

    "not log in user without password" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/login")
        .withFormUrlEncodedBody("email" -> "doe@example.com")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"password_error")
    }

    "not log in user with invalid password" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "email" -> userA.email,
        "password" -> "totally wrong password")
      (userRepository.findByEmail _) when (userA.email) returns (Future.successful(Some(userA)))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Invalid email address or password.")
    }

    "not log in nonexistent user" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "email" -> "nonexisting@email.com",
        "password" -> userA.password)
      (userRepository.findByEmail _) when ("nonexisting@email.com") returns (Future.successful(None))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Invalid email address or password.")
    }

    "log in user" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest("POST", "/login").withFormUrlEncodedBody("email" -> userA.email, "password" -> "testPassword")
      (userRepository.findByEmail _) when (userA.email) returns (Future.successful(Some(userA.copy(id = Some(1)))))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.UserController.show(1).url))
      session(result).get("session.username") shouldBe defined
    }
  }
}
