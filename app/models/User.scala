package models

import play.api.db.slick.Config.driver.simple._

case class User(name: String, email: String, googleId: String, avatar: Option[String] = None, id: Option[Int] = None)

object User {
  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def name = column[String]("name", O.NotNull)
    def email = column[String]("email", O.NotNull)
    def googleId = column[String]("google_id", O.NotNull)
    def avatar = column[Option[String]]("avatar", O.Nullable)
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)

    def * = (name, email, googleId, avatar, id) <> ((User.apply _).tupled, User.unapply)

    def emailIndex = index("users_email_index", email, true)
    def googleIdIndex = index("users_google_id_index", googleId, true)
  }

  val table = TableQuery[UsersTable]
}


