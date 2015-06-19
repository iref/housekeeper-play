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
import slick.backend.DatabaseConfig
import slick.profile.BasicProfile

/**
 * Trait groups all necessary application components
 */
// TODO Right now SlickEvolutionsComponents are only in local play-slick:1.0.1-SNAPSHOT.
// So without building play-slick locally, it won't compile
// Should work after Issue #269 in play-slick is resolved and new artifact is published
trait HousekeeperComponent extends BuiltInComponents with SlickComponents
  with I18nComponents with Controllers with Repositories
  with EvolutionsComponents with SlickEvolutionsComponents {

  lazy val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] = api.dbConfig(DbName("default"))
  }

  lazy val router: Router = wire[Routes] withPrefix "/"

  lazy val assets: Assets = wire[Assets]

  lazy val webJarAssets: WebJarAssets = wire[WebJarAssets]

  lazy val dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  def onStart(): Unit = {
    applicationEvolutions
  }

  onStart()
}
