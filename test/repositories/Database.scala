package repositories

import play.api.db.DBApi
import play.api.db.evolutions.Evolutions

import test.HousekeeperSpec

/**
 * Helper trait for initializing database for Repository specs.
 */
trait Database { self: HousekeeperSpec =>

  protected def withDatabase(test: Repositories => Unit): Unit = {
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
