package test

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

/**
 * Provides preferred trait stack for all specifications.
 */
abstract class HousekeeperSpec
    extends WordSpec
    with BeforeAndAfterEach
    with Matchers
    with MockFactory
    with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
}
