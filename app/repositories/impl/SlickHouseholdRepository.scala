package repositories.impl

import models.Household
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import repositories.HouseholdRepository
import repositories.impl.tables.HouseholdsTable
import slick.driver.JdbcProfile

import scala.concurrent.Future

private[repositories] class SlickHouseholdRepository(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HouseholdRepository with HasDatabaseConfigProvider[JdbcProfile] with HouseholdsTable {

  import driver.api._

  def find(id: Int): Future[Option[Household]] = {
    val query = households.filter(_.id === id)
    db.run(query.result.headOption)
  }

  def save(household: Household): Future[Household] = ???
}
