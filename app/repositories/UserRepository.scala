package repositories

import models.User
import play.api.db.slick.Config.driver.simple._

/**
 * Provides access to users data store.
 */
class UserRepository {

  def find(id: Int)(implicit s: Session): Option[User] =
    User.table.filter(_.id === id).firstOption

  def findByGoogleId(googleId: String)(implicit s: Session): Option[User] =
    User.table.filter(_.googleId === googleId).firstOption

  def all(implicit s: Session): List[User] = User.table.list

  def save(user: User)(implicit s: Session): Option[Int] =
    (User.table returning User.table.map(_.id)) += user

  def remove(id: Int)(implicit s: Session): Unit =
    User.table.filter(_.id === id).delete

}
