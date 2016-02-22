package global

import com.softwaremill.macwire.MacwireMacros._
import controllers.{Assets, Controllers, WebJarAssets}
import play.api.BuiltInComponents
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.{DbName, SlickComponents}
import play.api.i18n.I18nComponents
import play.api.routing.Router
import repositories.Repositories
import router.Routes
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * Trait groups all necessary application components
 */
trait HousekeeperComponent
  extends BuiltInComponents
  with SlickComponents
  with I18nComponents
  with Controllers
  with Repositories
  with EvolutionsComponents
  with SlickEvolutionsComponents {

  lazy val dbConfig: DatabaseConfig[JdbcProfile] = api.dbConfig(DbName("default"))

  lazy val router: Router = wire[Routes] withPrefix "/"

  lazy val assets: Assets = wire[Assets]

  lazy val webJarAssets: WebJarAssets = wire[WebJarAssets]

  lazy val dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  def onStart(): Unit = {
    applicationEvolutions
  }

  onStart()
}
