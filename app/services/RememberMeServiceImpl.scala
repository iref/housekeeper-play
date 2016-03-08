package services

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import net.ceedubs.ficus.Ficus._
import play.api.Configuration

import scala.concurrent.duration.FiniteDuration

class RememberMeServiceImpl(clock: Clock, configuration: Configuration) extends RememberMeService {

  override def remember(authenticator: CookieAuthenticator, rememberMe: Boolean): CookieAuthenticator = {
    val c = configuration.underlying
    if (rememberMe) {
      authenticator.copy(
        expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
        idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
        cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
      )
    } else {
      authenticator
    }
  }
}
