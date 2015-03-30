package models

import org.specs2.mutable.Specification
import play.api.db.slick.Config.driver.simple._

class UserRepositorySpec extends Specification with Database {

  val userRepository = new UserRepository()

  val userA = User("John Doe", "doe@example.com", "test_password")

  "UserRepository" should {

    "save new user" in withDatabase { implicit session =>
      // when
      val user = userRepository.save(userA)

      // then
      val Some(found) = User.table.filter(_.id === user.id).firstOption
      user.id must beEqualTo(found.id)
      found.name must beEqualTo(userA.name)
      found.email must beEqualTo(userA.email)
      found.password must beEqualTo(userA.password)
    }

    "find existing user by id" in withDatabase { implicit session =>
      // given
      val Some(userId) = (User.table returning User.table.map(_.id)) += userA

      // when
      val Some(found) = userRepository.find(userId)

      // then
      found.id must beSome(userId)
      found.name must beEqualTo(userA.name)
      found.email must beEqualTo(userA.email)
      found.password must beEqualTo(userA.password)
    }

    "find nonexistent user returns None" in withDatabase { implicit session =>
      // given
      User.table += userA

      // when
      val notFound = userRepository.find(Int.MaxValue)

      // then
      notFound must beNone
    }

  }

}
