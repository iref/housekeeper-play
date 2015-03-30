import com.softwaremill.macwire.MacwireMacros._
import controllers.{UserController, ShoppingListItemController, ShoppingListController}
import models.Repositories

/**
 * Bootstrapping of application components.
 */
object Bootstrap extends Repositories {

  lazy val shoppingListController = wire[ShoppingListController]

  lazy val shoppingListItemController = wire[ShoppingListItemController]

  lazy val userController = wire[UserController]
}
