package controllers

import controllers.UserController.Registration
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mutable.Specification

class RegistrationSpec extends Specification {

  "Registration" should {
    "create user with hashed password" in {
      // given
      val registration = Registration("John Doe", "doe@example.com", "testPassword", "testPassword")

      // when
      val user = registration.toUser

      // then
      user.id must beNone
      user.email must beEqualTo("doe@example.com")
      user.name must beEqualTo("John Doe")
      BCrypt.checkpw("testPassword", user.password) must beTrue
    }
  }

}
