package repositories.impl.tables

import models.Household
import slick.driver.JdbcProfile
import slick.profile.SqlProfile

private[impl] trait HouseholdsTable extends UsersTable {
  protected val driver: JdbcProfile

  import driver.api._

  class Households(tag: Tag) extends Table[Household](tag, "households") {
    import SqlProfile.{ColumnOption => Constraints}

    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", Constraints.NotNull)
    def description = column[Option[String]]("description", Constraints.Nullable)
    def logo = column[Option[String]]("logo", Constraints.Nullable)
    def ownerId = column[Option[Int]]("owner_id", Constraints.NotNull)

    def owner = foreignKey("household_owner_fk", ownerId, users)(_.id)

    def * = (name, description, logo, ownerId, id) <> ((Household.apply _).tupled, Household.unapply)
  }

  protected val households = TableQuery[Households]
}
