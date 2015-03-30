package models

import play.api.db.slick.Config.driver.simple._

case class User(name: String, email: String, password: String, id: Option[Int] = None)

object User {

  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def name = column[String]("name", O.NotNull)
    def email = column[String]("email", O.NotNull)
    def password = column[String]("password", O.NotNull)
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)

    def * = (name, email, password, id) <> ((User.apply _).tupled, User.unapply)

    def emailIndex = index("users_email_index", email, true)
  }

  val table = TableQuery[UsersTable]
}

class UserRepository {

  def save(user: User)(implicit session: Session): User = {
    val q = (User.table returning User.table.map(_.id)) into {
      case (row, userId) => user.copy(id = userId)
    }
    q += user
  }

  def find(id: Int)(implicit session: Session): Option[User] = {
    User.table.filter(_.id === id).firstOption
  }

}
