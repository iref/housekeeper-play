package repositories.impl.tables

import models.Household
import slick.driver.JdbcProfile
import slick.lifted.MappedProjection
import slick.profile.SqlProfile

private[impl] trait HouseholdsTable extends UsersTable {
  protected val driver: JdbcProfile

  import driver.api._

  class Households(tag: Tag) extends Table(tag, "households") {
    import SqlProfile.{ColumnOption => Constraints}

    def id = column[Option[Int]]("id", O.PrimaryKey)
    def name = column[String]("name", Constraints.NotNull)
    def description = column[Option[String]]("description", Constraints.Nullable)
    def logo = column[Option[String]]("logo", Constraints.Nullable)
    def ownerId = column[Option[Int]]("owner_id", Constraints.NotNull)

    def owner = foreignKey("household_owner_fk", ownerId, users)(_.id)

    def * : MappedProjection[Household, (String, Option[String], Option[String], Option[Int], Option[Int])] = (name, description, logo, ownerId, id) <> ((Household.apply _).tupled, Household.unapply)
  }

  protected val households = TableQuery[Households]
}
