package global

import com.softwaremill.macwire._
import play.api.ApplicationLoader.Context
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}

import controllers.{Assets, Controllers}
import repositories.Repositories
import router.Routes

/**
 * Loader, that loads all necessary application resource and wires them together
 */
class HousekeeperApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    new HousekeeperApplication(context).application
  }
}

class HousekeeperApplication(context: Context)
    extends BuiltInComponentsFromContext(context)
    with EvolutionsComponents
    with SlickEvolutionsComponents
    with Repositories
    with Controllers {

  lazy val router: Router = {
    val prefix: String = "/"
    wire[Routes]
  }

  lazy val assets: Assets = wire[Assets]

  lazy val dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  def onStart() = {
    LoggerConfigurator(environment.classLoader).foreach {
      _.configure(environment)
    }

    applicationEvolutions
  }

  onStart()
}
