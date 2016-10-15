package repositories

import org.scalatest.Outcome
import play.api.db.evolutions.Evolutions
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.{DbName, SlickComponents}
import play.api.inject.DefaultApplicationLifecycle
import play.api.{Configuration, Environment}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import test.HousekeeperSpec

/**
 * Helper trait for initializing database for Repository specs.
 */
trait Database
    extends SlickComponents
    with SlickEvolutionsComponents
    with Repositories { self: HousekeeperSpec =>

  lazy val applicationLifecycle = new DefaultApplicationLifecycle

  lazy val environment: Environment = Environment.simple()

  lazy val configuration: Configuration = Database.testConfiguration

  lazy val dbConfig: DatabaseConfig[JdbcProfile] = api.dbConfig(DbName("default"))

  override protected def withFixture(test: NoArgTest): Outcome = {
    try {
      setupSchema()
      test()
    } finally {
      dropSchema()
    }
  }

  def dropSchema(): Unit = {
    val db = dbApi.database("default")
    Evolutions.cleanupEvolutions(db)
  }

  def setupSchema(): Unit = {
    val db = dbApi.database("default")
    Evolutions.applyEvolutions(db)
  }
}

object Database {
  def testConfiguration: Configuration = Configuration.reference ++ Configuration.from(
    Map(
      "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.default.db.driver" -> "org.h2.Driver",
      "slick.dbs.default.db.url" -> ("jdbc:h2:mem:play-test-" + scala.util.Random.nextInt),
      "slick.dbs.default.db.user" -> "sa",
      "slick.dbs.default.db.password" -> ""
    )
  )
}
