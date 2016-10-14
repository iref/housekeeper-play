package controllers

import models.User
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Matchers
import org.specs2.execute.{AsResult, Result}
import org.specs2.mock.Mockito
import play.api.Application
import play.api.test.{FakeRequest, PlaySpecification}
import repositories.UserRepository

import scala.concurrent.Future

class SessionControllerSpec extends PlaySpecification with Mockito {
  val userA = User("John Doe", "doe@example.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()))

  def withUserRepository[T: AsResult](f: (UserRepository, Application) => T): Result = {
    WithControllers.running((components, app) => f(components.userRepository, app))
  }

  "#login" should {

    "render login template" in WithControllers.running { (_, app) =>
      // when
      val Some(result) = route(app, FakeRequest(GET, "/login"))

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Please, sign in.")
    }
  }

  "#logout" should {

    "remove username from session" in WithControllers.running { (_, app) =>
      // given
      val request = FakeRequest(GET, "/logout").withSession(("session.username", "1"))

      // when
      val Some(result) = route(app, request)

      // then
      session(result).isEmpty must beTrue
    }

    "redirect to home page" in WithControllers.running { (_, app) =>
      // given
      val request = FakeRequest(GET, "/logout").withSession("session.username" -> "1")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome("/")
    }
  }

  "#authenticate" should {

    "not log in user without email" in WithControllers.running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody("password" -> "testPassword")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"email_error")
    }

    "not log in user without password" in WithControllers.running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody("email" -> "doe@example.com")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"password_error")
    }

    "not log in user with invalid password" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "email" -> userA.email,
        "password" -> "totally wrong password")
      userRepository.findByEmail(userA.email) returns (Future.successful(Some(userA)))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Invalid email address or password.")
    }

    "not log in nonexistent user" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "email" -> "nonexisting@email.com",
        "password" -> userA.password)
      userRepository.findByEmail("nonexisting@email.com") returns (Future.successful(None))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Invalid email address or password.")
    }

    "log in user" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest("POST", "/login").withFormUrlEncodedBody("email" -> userA.email, "password" -> "testPassword")
      userRepository.findByEmail(Matchers.eq(userA.email)) returns (Future.successful(Some(userA.copy(id = Some(1)))))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(1).url)
      session(result).get("session.username") must beSome
    }
  }
}
