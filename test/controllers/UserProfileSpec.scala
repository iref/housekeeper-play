package controllers

import controllers.UserController.UserProfile
import models.User
import org.specs2.mutable.Specification
import play.api.libs.Codecs

class UserProfileSpec extends Specification {

  val user = User("John Doe", "doe@example.com", "testPassword", Some(1))

  "UserProfile" should {
    "create correct profile from user" in {
      val profile = UserProfile(user)

      profile.name must beEqualTo(user.name)
      profile.gravatar must not beEmpty
    }

    "create correct gravatar url" in {
      val expectedHash = Codecs.md5(user.email.getBytes("UTF-8"))
      val expectedUrl = s"https://secure.gravatar.com/avatar/$expectedHash?s=200"

      val gravatar = UserProfile(user).gravatar

      gravatar must beEqualTo(expectedUrl)
    }
  }

}
