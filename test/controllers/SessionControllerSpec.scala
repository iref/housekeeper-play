package controllers

import models.{User, UserRepository}
import org.mindrot.jbcrypt.BCrypt
import org.mockito.{Mockito => m, Matchers}
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.db.slick._
import play.api.test.{FakeRequest, WithApplication, PlaySpecification}

class SessionControllerSpec extends PlaySpecification with Mockito with BeforeEach {
  val userA = User("John Doe", "doe@example.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()))

  val userRepository = mock[UserRepository]

  val controller = new SessionController(userRepository)

  override def before: Any = m.reset(userRepository)

  "#login" should {

    "render login template" in new WithApplication {
      // when
      val result = controller.login()(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Please, sign in.")
    }
  }

  "#authenticate" should {

    "not log in user without email" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("password" -> "testPassword")

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"email_error")
    }

    "not log in user without password" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> "doe@example.com")

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"password_error")
    }

    "not log in user with invalid password" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> userA.email,
        "password" -> "totally wrong password")
      userRepository.findByEmail(Matchers.eq(userA.email))(any[Session]) returns(Some(userA))

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Invalid email address or password.")
    }

    "not log in nonexistent user" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> "nonexisting@email.com",
        "password" -> userA.password)
      userRepository.findByEmail(Matchers.eq("nonexisting@email.com"))(any[Session]) returns(None)

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Invalid email address or password.")
    }

    "log in user" in new WithApplication {
      // given
      val request = FakeRequest().withFormUrlEncodedBody("email" -> userA.email, "password" -> "testPassword")
      userRepository.findByEmail(Matchers.eq(userA.email))(any[Session]) returns(Some(userA.copy(id = Some(1))))

      // when
      val result = controller.authenticate()(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(1).url)
      session(result).get("session.username") must beSome
    }
  }
}
