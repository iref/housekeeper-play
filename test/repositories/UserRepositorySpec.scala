package repositories

import models.User
import org.specs2.mutable.Specification
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import repositories.UserRepository

class UserRepositorySpec extends Specification with Database {

  val userA = User("Jan", "hacker@hacker.com", "1234")

  "UserRepository" should {

    "find user by id" in withDatabase { implicit session =>
      // given
      val userId = (User.table returning User.table.map(_.id)) += userA

      // when
      val Some(found) = UserRepository.find(userId.get)

      // then
      found.id must beSome[Int]
      found.name must be(userA.name)
      found.email must be(userA.email)
      found.googleId must be(userA.googleId)
      found.avatar must beNone
    }

    "not find user by nonexistent id" in withDatabase { implicit session =>
      // given
      val nonexistentId: Int = 1

      // when
      val notFound = UserRepository.find(nonexistentId)

      // then
      notFound must beNone
    }

    "find user by her google id" in withDatabase { implicit session =>
      // given
      User.table += userA

      // when
      val Some(found) = UserRepository.findByGoogleId(userA.googleId)

      // then
      found.avatar must beEqualTo(userA.avatar)
      found.email must beEqualTo(userA.email)
      found.googleId must beEqualTo(userA.googleId)
      found.name must beEqualTo(userA.name)
      found.id must beSome[Int]
    }

    "not find any user for nonexistent google id" in withDatabase { implicit session =>
      // given
      val googleId = "XXXX1234"

      // when
      val notFound = UserRepository.findByGoogleId(googleId)

      // then
      notFound must beNone
    }

    "find all users" in withDatabase { implicit session =>
      // given
      val users = Seq(
        userA,
        User("Anka", "Hacker2@example.com", "5678", Some("myavatar"))
      )
      User.table ++= users

      // when
      val all = UserRepository.all

      // then
      all must have size(users.size)
    }

    "save new user" in withDatabase { implicit session =>
      // given
      val newUser = User("NewUser", "newuser@example.com", "XX12345", Some("newuser.avatar.com"))

      // when
      val Some(newUserId) = UserRepository.save(newUser)

      // then
      val Some(saved) = User.table.filter(_.id === newUserId).firstOption
      saved.avatar must beEqualTo(newUser.avatar)
      saved.email must beEqualTo(newUser.email)
      saved.name must beEqualTo(newUser.name)
      saved.googleId must beEqualTo(newUser.googleId)
    }

    "delete user by given id" in withDatabase { implicit session =>
      // given
      val userId = (User.table returning User.table.map(_.id)) += userA

      // when
      UserRepository.remove(userId.get)

      // then
      User.table.filter(_.id === userId).firstOption must beNone
    }

    "not delete any user with nonexistent id" in withDatabase { implicit session =>
      // given
      val userId: Int = 1
      val userCount = User.table.length.run

      // when
      UserRepository.remove(userId)

      // then
      User.table.length.run must beEqualTo(userCount)
    }
  }
}
