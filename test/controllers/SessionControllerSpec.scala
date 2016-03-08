package controllers

import com.mohiva.play.silhouette.api.util.JsonFormats._
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.test._
import global.HousekeeperComponent
import models.User
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Matchers
import org.specs2.mock.Mockito
import play.api.libs.Crypto
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.{FakeRequest, PlaySpecification, WithApplicationLoader}
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import repositories.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag

class SessionControllerSpec extends PlaySpecification with Mockito {
  val userA = User("John Doe", "doe@example.com", BCrypt.hashpw("testPassword", BCrypt.gensalt()))

  val userRepositoryMock = mock[UserRepository]

  val passwordInfoRepositoryMock = mock[DelegableAuthInfoDAO[PasswordInfo]]
  passwordInfoRepositoryMock.classTag returns(ClassTag(classOf[PasswordInfo]))

  implicit val authEnv = FakeEnvironment[User, CookieAuthenticator](Seq(userA.loginInfo -> userA))

  class WithController extends WithApplicationLoader(applicationLoader = new SessionControllerApplicationLoader)

  class SessionControllerApplicationLoader extends ApplicationLoader {
    override def load(context: ApplicationLoader.Context): Application = {
      (new BuiltInComponentsFromContext(context) with SessionControllerComponents).application
    }
  }

  trait SessionControllerComponents extends HousekeeperComponent {
    override lazy val userRepository: UserRepository = userRepositoryMock

    override lazy val passwordInfoRepository = passwordInfoRepositoryMock

    override lazy val env = authEnv
  }

  "#login" should {

    "render login template" in new WithController {
      // when
      val Some(result) = route(FakeRequest(GET, "/login"))

      // then
      status(result) must beEqualTo(OK)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("Please, sign in.")
    }
  }

  "#logout" should {

    "remove username from session" in new WithController {
      // given
      val request = FakeRequest(GET, "/logout").withAuthenticator(userA.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      session(result).isEmpty must beTrue
    }

    "redirect to login page" in new WithController {
      // given
      val request = FakeRequest(GET, "/logout").withAuthenticator(userA.loginInfo)

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome("/login")
    }
  }

  "#authenticate" should {

    passwordInfoRepositoryMock.find(userA.loginInfo) returns(Future.successful(Option(userA.passwordInfo)))

    "not log in user without email" in new WithController {
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody("password" -> "testPassword")

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"email_error")
    }

    "not log in user without password" in new WithController {
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody("email" -> "doe@example.com")

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(BAD_REQUEST)
      contentType(result) must beSome("text/html")
      contentAsString(result) must contain("span id=\"password_error")
    }

    "not log in user with invalid password" in new WithController {
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody("email" -> userA.email,
        "password" -> "totally wrong password")
      userRepositoryMock.findByEmail(userA.email) returns(Future.successful(Some(userA)))

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome("/login")
      flash(result).get("error") must beSome("invalid.credentials")
    }

    "not log in nonexistent user" in new WithController {
      // given
      val request = FakeRequest(POST, "/login").withFormUrlEncodedBody("email" -> "nonexisting@email.com",
        "password" -> userA.password)
      userRepositoryMock.findByEmail("nonexisting@email.com") returns(Future.successful(None))
      passwordInfoRepositoryMock.find(userA.copy(email = "nonexisting@email.com").loginInfo) returns(Future.successful(None))

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome("/login")
    }

    "log in user" in new WithController {
      // given
      val request = FakeRequest("POST", "/login").withFormUrlEncodedBody("email" -> userA.email, "password" -> "testPassword")
      userRepositoryMock.findByEmail(Matchers.eq(userA.email)) returns(Future.successful(Some(userA.copy(id = Some(1)))))
      passwordInfoRepositoryMock.find(userA.loginInfo) returns(Future.successful(Option(userA.passwordInfo)))

      // when
      val Some(result) = route(request)

      // then
      status(result) must beEqualTo(SEE_OTHER)
      redirectLocation(result) must beSome(routes.UserController.show(1).url)
      cookies(result).get("id") must beSome[Cookie]
    }

    "remember user if rememberMe is checked" in new WithController {
      // given
      val request = FakeRequest("POST", "/login").withFormUrlEncodedBody(
        "email" -> userA.email,
        "password" -> "testPassword",
        "rememberMe" -> "true"
      )
      userRepositoryMock.findByEmail(Matchers.eq(userA.email)) returns(Future.successful(Some(userA.copy(id = Some(1)))))
      passwordInfoRepositoryMock.find(userA.loginInfo) returns(Future.successful(Option(userA.passwordInfo)))

      // when
      val Some(result) = route(request)

      // then
      val c = cookies(result)
      c.get("id") match {
        case Some(cookie) => {
          Json.parse(Crypto.decryptAES(cookie.value)).asOpt(Json.reads[CookieAuthenticator]) match {
            case Some(auth) => auth.cookieMaxAge must beSome(30.days)
            case None => failure("Authenticator cookie is not valid.")
          }
        }
        case None => failure("Authenticator cookie not set")
      }

    }
  }
}
