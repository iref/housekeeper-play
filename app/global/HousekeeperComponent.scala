package global

import com.softwaremill.macwire.MacwireMacros._
import controllers.{Assets, Controllers, WebJarAssets}
import play.api.BuiltInComponents
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickComponents}
import play.api.i18n.I18nComponents
import play.api.routing.Router
import repositories.Repositories
import router.Routes
import services.ServiceComponents
import slick.backend.DatabaseConfig
import slick.profile.BasicProfile

/**
 * Trait groups all necessary application components
 */
trait HousekeeperComponent
  extends BuiltInComponents
  with SlickComponents
  with EvolutionsComponents
  with SlickEvolutionsComponents
  with I18nComponents
  with SilhouetteComponents
  with Controllers
  with Repositories
  with ServiceComponents {

  lazy val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] = api.dbConfig(DbName("default"))
  }

  lazy val router: Router = wire[Routes] withPrefix "/"

  lazy val assets: Assets = wire[Assets]

  lazy val webJarAssets: WebJarAssets = wire[WebJarAssets]

  lazy val dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  def onStart() = {
    applicationEvolutions
  }

  onStart()
}
