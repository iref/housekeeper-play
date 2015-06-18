package models

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class ShoppingList(title: String, description: Option[String] = None, id: Option[Int] = None)

case class ShoppingListDetail(shoppingList: ShoppingList, items: Seq[ShoppingListItem])


