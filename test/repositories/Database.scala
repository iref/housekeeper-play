package repositories

import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple.Session
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Base trait with database helpers for all database tests.
 */
trait Database {

  /**
   * Runs test within fake play application with initialized in-memory database.
   *
   * @param test test function, that should be run.
   * @tparam T return value of test. This is usually result of test
   * @return result of test
   */
  def withDatabase[T](test: Session => T): T =
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      DB.withSession { implicit session =>
        test(session)
      }
    }
}
