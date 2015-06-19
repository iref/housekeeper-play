package global

import play.api.ApplicationLoader.Context
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}

/**
 * Loader, that loads all necessary application resource and wires them together
 */
class HousekeeperApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    (new BuiltInComponentsFromContext(context) with HousekeeperComponent).application
  }
}
