package repositories.impl

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import models.User
import play.api.db.slick.HasDatabaseConfig
import repositories.UserRepository
import repositories.impl.tables.UsersTable
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

private[repositories] class SlickUserRepository(protected val dbConfig: DatabaseConfig[JdbcProfile])
  extends HasDatabaseConfig[JdbcProfile] with UserRepository with UsersTable {

  import driver.api._

  def all: Future[List[User]] = db.run(users.result).map(_.toList)

  def save(user: User): Future[User] = {
    val q = (users returning users.map(_.id)) into {
      case (row, userId) => user.copy(id = userId)
    }

    val q2 = q += user
    db.run(q2.transactionally)
  }

  def update(user: User): Future[Int] = {
    val query = users.filter(_.id === user.id).update(user)
    db.run(query.transactionally)
  }

  def find(id: Int): Future[Option[User]] = {
    val query = users.filter(_.id === id)
    db.run(query.result.headOption)
  }

  def findByEmail(email: String): Future[Option[User]] = {
    val query = users.filter(_.email === email)
    db.run(query.result.headOption)
  }
}
