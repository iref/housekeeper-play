package repositories.impl.tables

import models.User
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

private[impl] trait UsersTable {
  protected val driver: JdbcProfile

  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def name = column[String]("name", ColumnOption.NotNull)
    def email = column[String]("email", ColumnOption.NotNull)
    def password = column[String]("password", ColumnOption.NotNull)
    def id = column[Option[Int]]("id", slick.ast.ColumnOption.PrimaryKey, slick.ast.ColumnOption.AutoInc)

    def * = (name, email, password, id) <> ((User.apply _).tupled, User.unapply)

    def emailIndex = index("users_email_index", email, true)
  }

  protected val users = TableQuery[Users]
}
