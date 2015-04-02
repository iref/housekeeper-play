package controllers

import models.{User, UserRepository}
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Matchers
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.db.slick.Session
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

class UserControllerSpec extends PlaySpecification with BeforeEach with Mockito {

  val userRepository = mock[UserRepository]

  val userA = User("John Doe", "doe@example.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()))

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
      userRepository.save(any[User])(any[Session]) returns(userA.copy(id = Some(2)))

      // when
      val result = controller.save()(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(2).url)
      flash(result).get("info") must beSome
    }
  }

  "#show" should {

    "render user detail template" in new WithApplication {
      // given
      userRepository.find(Matchers.eq(1))(any[Session]) returns(Some(userA.copy(id = Some(1))))

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("User profile")
      contentAsString(result) must contain(userA.name)
    }

    "redirect to index if user does not exists" in new WithApplication {
      // given
      userRepository.find(Matchers.eq(1))(any[Session]) returns(None)

      // when
      val result = controller.show(1)(FakeRequest())

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.Application.index().url)
      flash(result).get("error") must beSome
    }
  }

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

  "#edit" should {
    "render edit user template for existing user" in new WithApplication {
      // given
      userRepository.find(Matchers.eq(1))(any[Session]) returns(Some(userA.copy(id = Some(1))))

      // when
      val result = controller.edit(1)(FakeRequest())

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Edit profile")
    }

    "redirect to index if user with does not exist" in new WithApplication {
      // given
      userRepository.find(Matchers.eq(1))(any[Session]) returns(None)

      // when
      val result = controller.edit(1)(FakeRequest())

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.Application.index().url)
      flash(result).get("error") must beSome
    }
  }

}
