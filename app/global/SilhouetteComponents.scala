package global

import com.mohiva.play.silhouette.api.{EventBus, Environment}
import com.mohiva.play.silhouette.api.util.{PasswordInfo, Clock}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticatorSettings, CookieAuthenticatorService}
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.impl.util.{SecureRandomIDGenerator, DefaultFingerprintGenerator, BCryptPasswordHasher}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.Configuration
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by ferko on 4.3.16.
  */
trait SilhouetteComponents {

  def configuration: Configuration

  def userService: UserService

  def passwordInfoRepository: DelegableAuthInfoDAO[PasswordInfo]

  lazy val passwordHasher = new BCryptPasswordHasher()

  lazy val authInfoRepository = new DelegableAuthInfoRepository(passwordInfoRepository)

  lazy val credentialsProvider = new CredentialsProvider(authInfoRepository, passwordHasher, Seq(passwordHasher))

  lazy val authenticatorService = {
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    new CookieAuthenticatorService(
      config,
      None,
      new DefaultFingerprintGenerator(false),
      new SecureRandomIDGenerator(), Clock())
  }

  lazy val env = Environment(userService, authenticatorService, Seq(), EventBus())


}
