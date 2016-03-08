package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.joda.time.{Days, DateTime}
import org.specs2.mutable.Specification
import play.api.Configuration

import scala.concurrent.duration._


class RememberMeServiceSpec extends Specification {

  val testConfiguration = Configuration.reference ++ Configuration.from(
    Map(
      "silhouette.authenticator.rememberMe.cookieMaxAge" -> "30 days",
      "silhouette.authenticator.rememberMe.authenticatorIdleTimeout" -> "5 days",
      "silhouette.authenticator.rememberMe.authenticatorExpiry" -> "30 days"
    )
  )

  val credentials = LoginInfo(CredentialsProvider.ID, "foo@test.com")

  val service: RememberMeService = new RememberMeServiceImpl(Clock(), testConfiguration)

  implicit val dateTimeOrdering = Ordering.fromLessThan[DateTime]((x, y) => x.compareTo(y) < 0)

  "#remember" should {

    "set expiration if rememberMe is checked" in {
      val authenticator = CookieAuthenticator("TestAuthenticator", credentials, DateTime.now, DateTime.now ,None, None, None)

      val actual = service.remember(authenticator, true)

      val expirySeconds = 30.days.toSeconds.toInt

      actual match {
        case CookieAuthenticator(_, loginInfo, _, expirationDateTime, idleTimeout, cookieMaxAge, _) =>
          loginInfo must beEqualTo(credentials)
          expirationDateTime must beLessThanOrEqualTo(DateTime.now.plusSeconds(expirySeconds))
          expirationDateTime must beGreaterThan(DateTime.now.plusSeconds(expirySeconds - 1))
          cookieMaxAge must beSome(30 days)
          idleTimeout must beSome(5 days)
      }
    }

    "not set expiration if rememberMe is not checked" in {
      val expectedExpiration = DateTime.now
      val authenticator = CookieAuthenticator("TestAuthenticator", credentials, expectedExpiration, expectedExpiration ,None, None, None)

      val actual = service.remember(authenticator, false)

      actual match {
        case CookieAuthenticator(_, loginInfo, _, expirationDateTime, idleTimeout, cookieMaxAge, _) =>
          loginInfo must beEqualTo(credentials)
          expirationDateTime must beEqualTo(expectedExpiration)
          idleTimeout must beNone
          cookieMaxAge must beNone
      }
    }

  }

}
