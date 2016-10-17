package repositories

import play.api.db.DBApi
import play.api.db.evolutions.Evolutions

import test.{FakeApplication, HousekeeperSpec}

/**
 * Helper trait for initializing database for Repository specs.
 */
trait Database extends FakeApplication { self: HousekeeperSpec =>

  protected def withDatabase[T](test: Repositories => T): T = {
    running { (components, _) =>
      try {
        setupSchema(components.dbApi)
        test(components)
      } finally {
        dropSchema(components.dbApi)
      }
    }
  }

  private def dropSchema(dbApi: DBApi): Unit = {
    val db = dbApi.database("default")
    Evolutions.cleanupEvolutions(db)
  }

  private def setupSchema(dbApi: DBApi): Unit = {
    val db = dbApi.database("default")
    Evolutions.applyEvolutions(db)
  }
}
