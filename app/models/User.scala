package models

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{LoginInfo, Identity}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.BCryptPasswordHasher

case class User(name: String, email: String, password: String, id: Option[Int] = None) extends Identity {
  def loginInfo: LoginInfo = LoginInfo(CredentialsProvider.ID, email)
  def passwordInfo: PasswordInfo = PasswordInfo(BCryptPasswordHasher.ID, password)
}


