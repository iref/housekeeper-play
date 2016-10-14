package controllers

import com.softwaremill.macwire.wire
import org.specs2.execute.{AsResult, Result}
import org.specs2.mock.Mockito
import play.api._
import play.api.routing.Router
import play.api.test.Helpers
import repositories.{ShoppingListItemRepository, ShoppingListRepository, UserRepository}
import router.Routes

class TestApplication(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context)
    with Controllers
    with Mockito {

  lazy val assets: Assets = wire[Assets]

  lazy val router: Router = {
    val prefix: String = "/"
    wire[Routes]
  }

  override def userRepository: UserRepository = mock[UserRepository]

  override def shoppingListRepository: ShoppingListRepository = mock[ShoppingListRepository]

  override def shoppingListItemRepository: ShoppingListItemRepository = mock[ShoppingListItemRepository]
}

object WithControllers {

  def running[T: AsResult](f: (TestApplication, Application) => T): Result = {
    val context: ApplicationLoader.Context = ApplicationLoader.createContext(
      new Environment(
        new java.io.File("."),
        ApplicationLoader.getClass.getClassLoader, Mode.Test))
    val components = new TestApplication(context)

    Helpers.running(components.application)(AsResult.effectively(f(components, components.application)))
  }
}