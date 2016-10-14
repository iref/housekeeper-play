package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

abstract class ViewController(val messagesApi: MessagesApi, val webJarAssets: WebJarAssets) extends Controller with I18nSupport {

  implicit val assets: WebJarAssets = webJarAssets

}
