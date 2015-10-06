package repositories

import org.specs2.mutable.BeforeAfter
import play.api._
import play.api.db.evolutions.Evolutions
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickComponents}
import play.api.inject._
import slick.backend.DatabaseConfig
import slick.profile.BasicProfile

/**
 * Helper trait for initializing database for Repository specs.
 */
trait Database extends BeforeAfter with SlickComponents with SlickEvolutionsComponents with Repositories {
  lazy val applicationLifecycle = new DefaultApplicationLifecycle

  lazy val environment: Environment = Environment.simple()

  lazy val configuration: Configuration = Database.testConfiguration

  lazy val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] = api.dbConfig(DbName("default"))
  }

  def after = {
    dbApi.databases().foreach { db =>
      Evolutions.cleanupEvolutions(db, true)
      db.shutdown()
    }
  }
  def before: Any = {
    dbApi.databases().foreach { db =>
      Evolutions.applyEvolutions(db)
    }
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
