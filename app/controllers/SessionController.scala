package controllers

import com.google.common.base.Charsets
import play.api.libs.Codecs
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import scala.util.Random

object SessionController extends Controller {

  def signIn(code: String, state: String) = Action.async { request =>
    if (request.session.get("state").exists(_ == state)) {
      // clean state token and retrieve profile
      Future.successful(Ok("Hello").withNewSession)
    } else {
      Future.successful(BadRequest("Invalid request"))
    }
  }

  def register() = Action { request =>
    val salt = Random.nextInt()
    val state = Codecs.md5((System.currentTimeMillis().toString + salt.toString).getBytes(Charsets.UTF_8))

    val queryParams = Map(
      "client_id" -> Seq("541401950578-dusu1glvkt0d2k7sh36cmp26s3hqqrt4.apps.googleusercontent.com"),
      "scope" -> Seq("https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile"),
      "redirect_uri" -> Seq("http://localhost:9000/auth/callback"),
      "response_type" -> Seq("code"),
      "access_type" -> Seq("offline"),
      "state" -> Seq(state)
    )
    Redirect("https://accounts.google.com/o/oauth2/auth", queryParams).withSession(("state", state))
  }
}
