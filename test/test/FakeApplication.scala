package test

import java.io.File

import com.typesafe.config.ConfigFactory
import play.api._
import play.core.DefaultWebCommands
import play.api.test.Helpers

import global.HousekeeperApplication

/**
 * Fake application to execute tests in.
 */
trait FakeApplication {
  /**
   * Path to test configuration file.
   */
  private val testConfigPath = "conf/application-test.conf"

  /**
   * Creates new instance of application components.
   * Subclasses can use this to override different parts of application setup with their own implementations, e.g. mocks.
   *
   * @param context application context which app components should be created from
   */
  protected def applicationComponents(context: ApplicationLoader.Context): HousekeeperApplication =
    new HousekeeperApplication(context)

  /**
   * Creates new application loader context.
   */
  private def createApplicationContext = {
    val fromFile = Configuration(ConfigFactory.parseFile(new File(testConfigPath)))
    val configuration = Configuration.reference ++ fromFile

    ApplicationLoader.Context(
      new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test),
      None,
      new DefaultWebCommands,
      configuration)
  }

  /**
   * Helper method to run controller test with started application.
   * Gives access to application components and started application to callback.
   *
   * @param f the callback function that should be run with started application
   */
  def running[T](f: (HousekeeperApplication, Application) => T): T = {
    val components = applicationComponents(createApplicationContext)
    Helpers.running(components.application)(f(components, components.application))
  }

  /**
   * Runs block with started application.
   */
  def apply[T](block: => T): T = {
    running((_, _) => block)
  }
}

object FakeApp extends FakeApplication
