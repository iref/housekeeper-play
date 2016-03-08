package services

import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator


trait RememberMeService {

  def remember(authenticator: CookieAuthenticator, rememberMe: Boolean): CookieAuthenticator

}
