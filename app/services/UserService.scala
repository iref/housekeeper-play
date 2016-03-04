package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.services.IdentityService
import models.User

trait UserService extends IdentityService[User] {

  def save(user: User): Future[User]

  def find(id: Int): Future[Option[User]]

}
