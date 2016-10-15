package controllers

import com.softwaremill.macwire.wire
import org.scalamock.scalatest.MockFactory
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Environment, Mode}
import play.api.test.Helpers
import play.api.routing.Router

import repositories.{ShoppingListItemRepository, ShoppingListRepository, UserRepository}
import router.Routes
import test.HousekeeperSpec

/**
 * Extended [[HousekeeperSpec]] to unit test controllers.
 */
abstract class HousekeeperControllerSpec extends HousekeeperSpec with MockFactory {

  /**
   * Test application components for controller unit tests.
   *
   * This class provides mock services for controllers under test.
   */
  protected class TestApplication(context: ApplicationLoader.Context)
      extends BuiltInComponentsFromContext(context)
      with Controllers {

    lazy val assets: Assets = wire[Assets]

    lazy val router: Router = {
      val prefix: String = "/"
      wire[Routes]
    }

    lazy val userRepository: UserRepository = stub[UserRepository]

    lazy val shoppingListRepository: ShoppingListRepository = stub[ShoppingListRepository]

    lazy val shoppingListItemRepository: ShoppingListItemRepository = stub[ShoppingListItemRepository]
  }

  /**
   * Helper method to run controller test with started application.
   * Gives access to application components and started application to callback.
   *
   * @param f the function from test components and running application.
   */
  def running[T](f: (TestApplication, Application) => T): T = {
    val context: ApplicationLoader.Context = ApplicationLoader.createContext(
      new Environment(
        new java.io.File("."),
        ApplicationLoader.getClass.getClassLoader, Mode.Test))
    val components = new TestApplication(context)

    Helpers.running(components.application)(f(components, components.application))
  }
}
