package test

import java.io.File

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import play.api.test.Helpers
import play.api._
import play.core.DefaultWebCommands

import global.HousekeeperApplication

/**
 * Provides preferred trait stack for all specifications.
 */
abstract class HousekeeperSpec
    extends WordSpec
    with BeforeAndAfterEach
    with Matchers
    with ScalaFutures
    with PlayTestConfiguration {

  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  /**
   * Creates new instance of application components.
   * Subclasses can use this to override different parts of application setup with their own implementations, e.g. mocks.
   *
   * @param context application context which app components should be created from
   */
  protected def applicationComponents(context: ApplicationLoader.Context): HousekeeperApplication =
    new HousekeeperApplication(context)

  /**
   * Helper method to run controller test with started application.
   * Gives access to application components and started application to callback.
   *
   * @param f the function from test components and running application.
   */
  protected def running[T](f: (HousekeeperApplication, Application) => T): T = {
    val components = withTestApplicationContext(applicationComponents)
    Helpers.running(components.application)(f(components, components.application))
  }

}

/**
 * Mixin that provides application loader context with configuration loaded from test configuration file.
 */
trait PlayTestConfiguration {

  /**
   * Path to test configuration file.
   */
  private val testConfigPath = "conf/application-test.conf"

  /**
   * Creates new application components for test configuration.
   *
   * @param f factory method from configuration to some type T
   */
  protected def withTestConfiguration[T](f: Configuration => T): T = {
    val fromFile = Configuration(ConfigFactory.parseFile(new File(testConfigPath)))
    val configuration = Configuration.reference ++ fromFile
    f(configuration)
  }

  /**
   * Creates new application components for test application context.
   *
   * @param f factory method that created application components from context
   */
  protected def withTestApplicationContext[T <: BuiltInComponentsFromContext](f: ApplicationLoader.Context => T): T = {
    withTestConfiguration { configuration: Configuration =>
      val context: ApplicationLoader.Context = ApplicationLoader.Context(
        new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test),
        None,
        new DefaultWebCommands,
        configuration)
      f(context)
    }
  }

}