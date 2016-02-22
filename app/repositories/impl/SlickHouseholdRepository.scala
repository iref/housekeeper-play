package repositories.impl

import scala.concurrent.Future

import models.Household
import play.api.db.slick.HasDatabaseConfig
import repositories.HouseholdRepository
import repositories.impl.tables.HouseholdsTable
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

private[repositories] class SlickHouseholdRepository(
    protected val dbConfig: DatabaseConfig[JdbcProfile])
  extends HasDatabaseConfig[JdbcProfile]
  with HouseholdRepository
  with HouseholdsTable {

  import driver.api._

  def find(id: Int): Future[Option[Household]] = {
    val query = households.filter(_.id === id)
    db.run(query.result.headOption)
  }

  def save(household: Household): Future[Household] = {
    val returnIdQuery = (households returning households.map(_.id)) into {
      case (row, id) => household.copy(id = id)
    }
    val insert = returnIdQuery += household
    db.run(insert.transactionally)
  }
}
