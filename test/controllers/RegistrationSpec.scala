package controllers

import controllers.UserController.Registration
import org.mindrot.jbcrypt.BCrypt
import test.HousekeeperSpec

class RegistrationSpec extends HousekeeperSpec {

  "Registration" should {
    "create user with hashed password" in {
      // given
      val registration = Registration("John Doe", "doe@example.com", "testPassword", "testPassword")

      // when
      val user = registration.toUser

      // then
      user.id should be(None)
      user.email should be("doe@example.com")
      user.name should be("John Doe")
      BCrypt.checkpw("testPassword", user.password) should be(true)
    }
  }

}
