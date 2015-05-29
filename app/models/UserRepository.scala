package models

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

import scala.concurrent.Future

case class User(name: String, email: String, password: String, id: Option[Int] = None)

trait UserRepository { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def name = column[String]("name", ColumnOption.NotNull)
    def email = column[String]("email", ColumnOption.NotNull)
    def password = column[String]("password", ColumnOption.NotNull)
    def id = column[Option[Int]]("id", slick.ast.ColumnOption.PrimaryKey, slick.ast.ColumnOption.AutoInc)

    def * = (name, email, password, id) <> ((User.apply _).tupled, User.unapply)

    def emailIndex = index("users_email_index", email, true)
  }

}

class UserRepositoryImpl(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with UserRepository {

  import driver.api._

  private val users = TableQuery[UsersTable]

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
