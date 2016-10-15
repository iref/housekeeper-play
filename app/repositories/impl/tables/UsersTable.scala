package repositories.impl.tables

import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

import models.User

private[impl] trait UsersTable {
  protected val driver: JdbcProfile

  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def name = column[String]("name", ColumnOption.NotNull)
    def email = column[String]("email", ColumnOption.NotNull)
    def password = column[String]("password", ColumnOption.NotNull)
    def id = column[Option[Int]]("id", slick.ast.ColumnOption.PrimaryKey, slick.ast.ColumnOption.AutoInc)

    // scalastyle:off
    def * = (name, email, password, id) <> ((User.apply _).tupled, User.unapply)
    // scalastyle:on

    def emailIndex = index("users_email_index", email, true)
  }

  protected val users = TableQuery[Users]
}
