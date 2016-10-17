package test

import java.io.File

import com.typesafe.config.ConfigFactory
import play.api.i18n.I18nComponents
import play.api.{Configuration, Environment, Mode}

/**
 * Test I18n components provider.
 */
object I18nTestComponents extends I18nComponents {

  lazy val environment = new Environment(
    new java.io.File("."), this.getClass.getClassLoader, Mode.Test)

  lazy val configuration = Configuration.reference ++ Configuration(
    ConfigFactory.parseFile(new File("conf/application-test.conf")))
}
