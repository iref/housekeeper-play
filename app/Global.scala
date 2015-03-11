import com.softwaremill.macwire.{InstanceLookup, Macwire}
import play.api.GlobalSettings

object Global extends GlobalSettings with Macwire {

  val bootstrap = wiredInModule(Bootstrap)

  override def getControllerInstance[A](controllerClass: Class[A]): A =
    bootstrap.lookupSingleOrThrow(controllerClass)
}
