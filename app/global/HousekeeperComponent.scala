package global

import com.softwaremill.macwire.MacwireMacros._
import controllers.{Assets, Controllers, WebJarAssets}
import play.api.BuiltInComponents
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickComponents}
import play.api.i18n.I18nComponents
import play.api.inject.{NewInstanceInjector, SimpleInjector, Injector}
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.csrf.CSRFComponents
import play.filters.headers.{SecurityHeadersComponents, SecurityHeadersFilter}
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
  with CSRFComponents
  with SecurityHeadersComponents
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

  override lazy val httpFilters: Seq[EssentialFilter] = Seq(csrfFilter, securityHeadersFilter)

  // Workaround for issue #5517, otherwise csrf config can't be created in views
  // This should be fixed in 2.5, more info in #5526
  override lazy val injector: Injector = new SimpleInjector(NewInstanceInjector) +
    router + crypto + httpConfiguration + csrfConfig

  def onStart() = {
    applicationEvolutions
  }

  onStart()
}
