package controllers

import org.scalamock.scalatest.MockFactory
import play.api.ApplicationLoader
import play.api.ApplicationLoader.Context

import global.HousekeeperApplication
import repositories.{ShoppingListItemRepository, ShoppingListRepository, UserRepository}
import test.{HousekeeperSpec, PlayTestConfiguration}

/**
 * Extended [[HousekeeperSpec]] to unit test controllers.
 */
abstract class HousekeeperControllerSpec extends HousekeeperSpec with MockFactory with PlayTestConfiguration {

  /**
   * Test application components for controller unit tests.
   *
   * This class provides mock services for controllers under test.
   */
  protected class TestApplication(context: ApplicationLoader.Context) extends HousekeeperApplication(context) {

    override lazy val userRepository: UserRepository = stub[UserRepository]

    override lazy val shoppingListRepository: ShoppingListRepository = stub[ShoppingListRepository]

    override lazy val shoppingListItemRepository: ShoppingListItemRepository = stub[ShoppingListItemRepository]
  }

  override protected def applicationComponents(context: Context): HousekeeperApplication = {
    new TestApplication(context)
  }
}

