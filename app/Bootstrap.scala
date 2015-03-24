import com.softwaremill.macwire.MacwireMacros._
import controllers.{ShoppingListItemController, ShoppingListController}
import models.Repositories

/**
 * Bootstrapping of application components.
 */
object Bootstrap extends Repositories {

  lazy val shoppingListController = wire[ShoppingListController]

  lazy val shoppingListItemController = wire[ShoppingListItemController]
}
