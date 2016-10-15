package controllers

import controllers.UserController.UserProfile
import models.User
import play.api.libs.Codecs
import test.HousekeeperSpec

class UserProfileSpec extends HousekeeperSpec {

  val user = User("John Doe", "doe@example.com", "testPassword", Some(1))

  "UserProfile" should {
    "create correct profile from user" in {
      val profile = UserProfile(user)

      profile.name should be(user.name)
      profile.email should be(user.email)
      profile.gravatar should not be (empty)
    }

    "create correct gravatar url" in {
      val expectedHash = Codecs.md5(user.email.getBytes("UTF-8"))
      val expectedUrl = s"https://secure.gravatar.com/avatar/$expectedHash?s=200"

      val gravatar = UserProfile(user).gravatar

      gravatar should be(expectedUrl)
    }
  }

}
