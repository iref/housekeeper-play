package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.test._
import global.HousekeeperComponent
import models.User
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mock.Mockito
import play.api.ApplicationLoader.Context
import play.api.test.{FakeRequest, PlaySpecification, WithApplicationLoader}
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import repositories.UserRepository

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class UserControllerSpec extends PlaySpecification with Mockito {

  val userA = User("John Doe", "doe@example.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()), Some(1))

  implicit val authEnv = FakeEnvironment[User, CookieAuthenticator](Seq(userA.loginInfo -> userA))

  val userRepositoryMock = mock[UserRepository]

  class WithController extends WithApplicationLoader(applicationLoader = new UserControllerTestLoader)

  class UserControllerTestLoader extends ApplicationLoader {
    def load(context: Context): Application = {
      (new BuiltInComponentsFromContext(context) with UserControllerTestComponents).application
    }
  }

  trait UserControllerTestComponents extends HousekeeperComponent {
    override lazy val userRepository = userRepositoryMock

    override lazy val env = authEnv
  }

  "#register" should {

    "render new user template" in new WithController {
      // when
      val Some(result) = route(FakeRequest(GET, "/users/new"))

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Create new account")
    }
  }

  "#save" should {

    "not create user without name" in new WithController {
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody("email" -> userA.email,
        "password" -> userA.password, "passwordConfirmation" -> userA.password)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not create user without email" in new WithController {
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody("name" -> userA.name,
        "password" -> userA.password, "passwordConfirmation" -> userA.password)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"email_error")
    }

    "not create user without password" in new WithController {
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "passwordConfirmation" -> userA.password
      )

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"password_error")
    }

    "not create user without passwordConfirmation" in new WithController {
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> userA.password
      )

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"passwordConfirmation_error")
    }

    "not create user with password shorter than 6 characters" in new WithController {
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> "abcde",
        "passwordConfirmation" -> "abcde"
      )

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"password_error")
    }

    "not create user if password does not match confirmation" in new WithController {
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> userA.password,
        "passwordConfirmation" -> (userA.password + "XXX")
      )

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("div class=\"alert alert-danger")
    }

    "redirect to user detail after successful user creation" in new WithController {
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> userA.password,
        "passwordConfirmation" -> userA.password)
      userRepositoryMock.save(any[User]) returns Future.successful(userA.copy(id = Some(2)))

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(2).url)
    }
  }

  "#show" should {

    "render user detail template" in new WithController {
      // given
      val request = FakeRequest(GET, "/users/1").withAuthenticator[CookieAuthenticator](userA.loginInfo)
      userRepositoryMock.find(1) returns Future.successful(Some(userA))

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("User profile")
      contentAsString(result) must contain(userA.name)
    }

    "redirect to index if user does not exists" in new WithController {
      // given
      val request = FakeRequest(GET, "/users/1").withAuthenticator[CookieAuthenticator](userA.loginInfo)
      userRepositoryMock.find(1) returns Future.successful(None)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ApplicationController.index().url)
      flash(result).get("error") must beSome
    }
  }

  "#edit" should {
    "render edit user template for existing user" in new WithController {
      // given
      val request = FakeRequest(GET, "/users/1/edit").withAuthenticator[CookieAuthenticator](userA.loginInfo)
      userRepositoryMock.find(1) returns Future.successful(Some(userA))

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Edit profile")
    }

    "redirect to index if user with does not exist" in new WithController {
      // given
      val request = FakeRequest(GET, "/users/1/edit").withAuthenticator[CookieAuthenticator](userA.loginInfo)
      userRepositoryMock.find(1) returns Future.successful(None)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.ApplicationController.index().url)
      flash(result).get("error") must beSome
    }
  }

  "#update" should {

    "not update user without name" in new WithController {
      // given
      userRepositoryMock.find(1) returns Future.successful(Some(userA))
      val request = FakeRequest(POST, "/users/1/edit").
        withFormUrlEncodedBody("email" -> "test@example.com").
        withAuthenticator[CookieAuthenticator](userA.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"name_error")
    }

    "not update user without email" in new WithController {
      // given
      userRepositoryMock.find(1) returns Future.successful(Some(userA))
      val request = FakeRequest(POST, "/users/1/edit").
        withFormUrlEncodedBody("name" -> "test").
        withAuthenticator[CookieAuthenticator](userA.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"email_error")
    }

    "not update user password if confirmation is missing" in new WithController {
      // given
      userRepositoryMock.find(1) returns Future.successful(Some(userA))
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> "test",
        "email" -> "test@example.com",
        "password" -> "testPassword2",
        "oldPassword" -> "testPassword"
      ).withAuthenticator[CookieAuthenticator](userA.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"passwordConfirmation_error")
    }

    "not update user password if it does not match confirmation" in new WithController {
      // given
      userRepositoryMock.find(1) returns Future.successful(Some(userA))
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> "testPasswordXXX",
        "passwordConfirmation" -> userA.password
      ).withAuthenticator[CookieAuthenticator](userA.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("div class=\"alert alert-danger")
    }

    "update user in repository" in new WithController {
      // given
      userRepositoryMock.find(1) returns Future.successful(Some(userA))
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> "Updated user name",
        "email" -> "updated@example.com",
        "password" -> "newUpdatedPasswordXXX",
        "passwordConfirmation" -> "newUpdatedPasswordXXX"
      ).withAuthenticator[CookieAuthenticator](userA.loginInfo)
      val toUpdate = User("Updated user name", "updated@example.com", BCrypt.hashpw("newUpdatedPasswordXXX", BCrypt.gensalt()), Some(1))
      userRepositoryMock.update(toUpdate) returns Future.successful(1)

      // when
      val Some(result) = route(request)
      Await.ready(result, 1.second)

      // then
      there was one(userRepositoryMock).update(any[User])
    }

    "update user if password is not provided" in new WithController {
      // given
      userRepositoryMock.find(1) returns Future.successful(Some(userA))
      val expectedUser = User("Passwordless user update", "updated@example.com", userA.password, Some(1))
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> expectedUser.name,
        "email" -> expectedUser.email
      ).withAuthenticator[CookieAuthenticator](userA.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"password_error")
      contentAsString(result) must contain("span id=\"passwordConfirmation_error")
    }

    "redirect to user detail after successful update" in new WithController {
      // given
      userRepositoryMock.find(1) returns Future.successful(Some(userA))
      val name = "Updated user name"
      val email = "updated@example.com"
      val password = "newPassword"
      val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
      val id = Option(1)
      
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> name,
        "email" -> email,
        "password" -> password,
        "passwordConfirmation" -> password
      ).withAuthenticator[CookieAuthenticator](userA.loginInfo)
      userRepositoryMock.update(any[User]) returns Future.successful(1)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(1).url)
    }

  }

}
