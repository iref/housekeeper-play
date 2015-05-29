package global

import com.softwaremill.macwire.MacwireMacros._
import controllers.{Assets, Controllers, WebJarAssets}
import models.Repositories
import play.api.BuiltInComponents
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickComponents}
import play.api.i18n.I18nComponents
import play.api.routing.Router
import router.Routes
import slick.backend.DatabaseConfig
import slick.profile.BasicProfile

/**
 * Trait groups all necessary application components
 */
trait HousekeeperComponent extends BuiltInComponents with SlickComponents
  with I18nComponents with Controllers with Repositories {

  lazy val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] = api.dbConfig(DbName("default"))
  }

  lazy val router: Router = wire[Routes] withPrefix "/"

  lazy val assets: Assets = wire[Assets]

  lazy val webJarAssets: WebJarAssets = wire[WebJarAssets]
}
