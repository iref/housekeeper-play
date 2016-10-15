package test

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class HousekeeperSpec extends WordSpec with BeforeAndAfterEach with Matchers with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

}
