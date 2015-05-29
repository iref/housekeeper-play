package controllers

import models.{User, UserRepositoryImpl}
import org.mindrot.jbcrypt.BCrypt
import org.mockito.{Matchers, Mockito => m}
import org.specs2.mock.Mockito
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.test.{FakeRequest, PlaySpecification, WithApplicationLoader}

import scala.concurrent.Future

class SessionControllerSpec extends PlaySpecification with Mockito {
  val userA = User("John Doe", "doe@example.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()))


  trait WithController extends WithApplicationLoader {
    val userRepository = mock[UserRepositoryImpl]

    private val app2MessageApi = Application.instanceCache[MessagesApi]
    val controller = new SessionController(userRepository, app2MessageApi(app))
  }

  "#login" should {

    "render login template" in new WithController {
      // when
      val result = controller.login()(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Please, sign in.")
    }
  }

  "#logout" should {

    "remove username from session" in new WithController {
      // given
      val request = FakeRequest().withSession(("session.username", "1"))

      // when
      val result = controller.logout()(request)

      // then
      session(result).isEmpty must beTrue
    }

    "redirect to home page" in new WithController {
      // given
      val request = FakeRequest().withSession("session.username" -> "1")

      // when
      val result = controller.logout()(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome("/")
    }
  }

  "#authenticate" should {

    "not log in user without email" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("password" -> "testPassword")

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"email_error")
    }

    "not log in user without password" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> "doe@example.com")

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"password_error")
    }

    "not log in user with invalid password" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> userA.email,
        "password" -> "totally wrong password")
      userRepository.findByEmail(userA.email) returns(Future.successful(Some(userA)))

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Invalid email address or password.")
    }

    "not log in nonexistent user" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> "nonexisting@email.com",
        "password" -> userA.password)
      userRepository.findByEmail("nonexisting@email.com") returns(Future.successful(None))

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Invalid email address or password.")
    }

    "log in user" in new WithController {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> userA.email, "password" -> "testPassword")
      userRepository.findByEmail(Matchers.eq(userA.email)) returns(Future.successful(Some(userA.copy(id = Some(1)))))

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(1).url)
      session(result).get("session.username") must beSome
    }
  }
}
