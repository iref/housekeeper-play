package models

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

import scala.concurrent.Future

case class User(name: String, email: String, password: String, id: Option[Int] = None)

