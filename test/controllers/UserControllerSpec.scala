package controllers

import scala.concurrent.duration._
import scala.concurrent.Future

import org.mindrot.jbcrypt.BCrypt
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._

import models.User
import repositories.UserRepository

class UserControllerSpec extends HousekeeperControllerSpec {

  val userA = User("John Doe", "doe@example.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()))

  def withUserRepository[T](f: (UserRepository, Application) => T) = {
    running((components, app) => f(components.userRepository, app))
  }

  "#register" should {

    "render new user template" in running { (_, app) =>
      // when
      val Some(result) = route(app, FakeRequest(GET, "/users/new"))

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Create new account")
    }
  }

  "#create" should {

    "not create user without name" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "email" -> userA.email,
        "password" -> userA.password, "passwordConfirmation" -> userA.password)

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"name_error")
    }

    "not create user without email" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "password" -> userA.password, "passwordConfirmation" -> userA.password)

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"email_error")
    }

    "not create user without password" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "passwordConfirmation" -> userA.password
      )

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"password_error")
    }

    "not create user without passwordConfirmation" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> userA.password
      )

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"passwordConfirmation_error")
    }

    "not create user with password shorter than 6 characters" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> "abcde",
        "passwordConfirmation" -> "abcde"
      )

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"password_error")
    }

    "not create user if password does not match confirmation" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> userA.password,
        "passwordConfirmation" -> (userA.password + "XXX")
      )

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("div class=\"alert alert-danger")
    }

    "redirect to user detail after successful user creation" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(POST, "/users").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> userA.password,
        "passwordConfirmation" -> userA.password)
      (userRepository.save _) when (*) returns (Future.successful(userA.copy(id = Some(2))))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.UserController.show(2).url))
      flash(result).get("info") shouldBe defined
      session(result).get("session.username") shouldBe defined
    }
  }

  "#show" should {

    "render user detail template" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(GET, "/users/1")
      (userRepository.find _) when (1) returns (Future.successful(Some(userA.copy(id = Some(1)))))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("User profile")
      contentAsString(result) should include(userA.name)
    }

    "redirect to index if user does not exists" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(GET, "/users/1")
      (userRepository.find _) when (1) returns (Future.successful(None))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ApplicationController.index().url))
      flash(result).get("error") shouldBe defined
    }
  }

  "#edit" should {
    "render edit user template for existing user" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(GET, "/users/1/edit")
      (userRepository.find _) when (1) returns (Future.successful(Some(userA.copy(id = Some(1)))))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Edit profile")
    }

    "redirect to index if user with does not exist" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(GET, "/users/1/edit")
      (userRepository.find _) when (1) returns (Future.successful(None))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.ApplicationController.index().url))
      flash(result).get("error") shouldBe defined
    }
  }

  "#update" should {

    "not update user without name" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody("email" -> "test@example.com")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"name_error")
    }

    "not update user without email" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody("name" -> "test")

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"email_error")
    }

    "not update user password if confirmation is missing" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> "test",
        "email" -> "test@example.com",
        "password" -> "testPassword2",
        "oldPassword" -> "testPassword"
      )

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"passwordConfirmation_error")
    }

    "not update user password if it does not match confirmation" in running { (_, app) =>
      // given
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> userA.name,
        "email" -> userA.email,
        "password" -> "testPasswordXXX",
        "passwordConfirmation" -> userA.password
      )

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("div class=\"alert alert-danger")
    }

    "update user in repository" in withUserRepository { (userRepository, app) =>
      // given
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> "Updated user name",
        "email" -> "updated@example.com",
        "password" -> "newUpdatedPasswordXXX",
        "passwordConfirmation" -> "newUpdatedPasswordXXX"
      )
      (userRepository.update _) when (*) returns (Future.successful(1))

      // when
      val Some(result) = route(app, request)
      result.futureValue

      // then
      (userRepository.update _) verify (*)
    }

    "update user if password is not provided" in running { (_, app) =>
      // given
      val expectedUser = User("Passwordless user update", "updated@example.com", userA.password, Some(1))
      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> expectedUser.name,
        "email" -> expectedUser.email
      )

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(BAD_REQUEST)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("span id=\"password_error")
      contentAsString(result) should include("span id=\"passwordConfirmation_error")
    }

    "redirect to user detail after successful update" in withUserRepository { (userRepository, app) =>
      // given
      val name = "Updated user name"
      val email = "updated@example.com"
      val password = "newPassword"
      val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
      val id = Option(1)

      val request = FakeRequest(POST, "/users/1/edit").withFormUrlEncodedBody(
        "name" -> name,
        "email" -> email,
        "password" -> password,
        "passwordConfirmation" -> password)
      (userRepository.update _) when (*) returns (Future.successful(1))

      // when
      val Some(result) = route(app, request)

      // then
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.UserController.show(1).url))
    }

  }

}
