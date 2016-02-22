package repositories

import scala.concurrent.Future

import models.User

/**
 * Trait for accessing and manipulating users in data storage.
 */
trait UserRepository {

  def all: Future[List[User]]

  def save(user: User): Future[User]

  def update(user: User): Future[Int]

  def find(id: Int): Future[Option[User]]

  def findByEmail(email: String): Future[Option[User]]
}
