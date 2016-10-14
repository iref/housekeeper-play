package models

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import repositories.impl.tables.ShoppingListTable
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption

import scala.concurrent.Future

case class ShoppingListItem(name: String, quantity: Int, priceForOne: Option[BigDecimal] = None,
  shoppingListId: Option[Int] = None, id: Option[Int] = None)

