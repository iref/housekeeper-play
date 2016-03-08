package services

import com.mohiva.play.silhouette.api.Authenticator
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator


trait RememberMeService {

  def remember(authenticator: CookieAuthenticator, rememberMe: Boolean): Authenticator

}
