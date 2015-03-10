package models

import play.api.Play.current
import play.api.db.slick.{DB, Session}
import play.api.db.slick.Config.driver.simple._
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Mix in this trait for tests, that require database access.
 */
trait Database {

  def withDatabase[T](test: Session => T): T =
   running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
     DB.withSession { implicit session =>
       test(session)
     }
   }

}
