package models

import org.specs2.mutable.Specification
import play.api.Application
import play.api.test.WithApplicationLoader
import scala.concurrent.Await
import scala.concurrent.duration._

class UserRepositorySpec extends Specification {

  val userA = User("John Doe", "doe@example.com", "test_password")
  
  trait WithRepository extends WithApplicationLoader {
    private val app2Repository = Application.instanceCache[UserRepositoryImpl]
    val userRepository = app2Repository(app)
  }

  "UserRepository" should {

    "save new user" in new WithRepository {
      // when
      val Some(id) = Await.result(userRepository.save(userA), 1.second).id

      // then
      val Some(found) = Await.result(userRepository.find(id), 1.second)
      found.id must beSome(id)
      found.name must beEqualTo(userA.name)
      found.email must beEqualTo(userA.email)
      found.password must beEqualTo(userA.password)
    }

    "return all users" in new WithRepository {
      // when
      Await.ready(userRepository.save(userA), 1.second)

      val users = Await.result(userRepository.all, 1.second)
      users must have size(1)
    }

    "find existing user by id" in new WithRepository {
      // given
      val Some(userId) = Await.result(userRepository.save(userA), 1.second).id

      // when
      val Some(found) = Await.result(userRepository.find(userId), 1.second)

      // then
      found.id must beSome(userId)
      found.name must beEqualTo(userA.name)
      found.email must beEqualTo(userA.email)
      found.password must beEqualTo(userA.password)
    }

    "find nonexistent user returns None" in new WithRepository {
      // given
      Await.ready(userRepository.save(userA), 1.second)

      // when
      val notFound = Await.result(userRepository.find(Int.MaxValue), 1.second)

      // then
      notFound must beNone
    }

    "find existing user by email" in new WithRepository {
      // given
      val Some(userId) = Await.result(userRepository.save(userA), 1.second).id

      // when
      val Some(found) = Await.result(userRepository.findByEmail(userA.email), 1.second)

      // then
      found.id must beSome(userId)
      found.name must beEqualTo(userA.name)
      found.email must beEqualTo(userA.email)
      found.password must beEqualTo(userA.password)
    }

    "findByEmail returns None if user does not exist" in new WithRepository {
      // given
      Await.ready(userRepository.save(userA), 1.second)

      // when
      val notFound = Await.result(userRepository.findByEmail("nonexisting@email.com"), 1.second)

      // then
      notFound must beNone
    }

    "update existing user" in new WithRepository {
      // given
      val Some(userId) = Await.result(userRepository.save(userA), 1.second).id
      val toUpdate = userA.copy(id = Some(userId), name = "New awesome name", email = "new@example.com", password = "newTestPassword")

      // when
      Await.ready(userRepository.update(toUpdate), 1.second)

      // then
      val Some(updated) = Await.result(userRepository.find(userId), 1.second)
      updated.email must beEqualTo("new@example.com")
      updated.name must beEqualTo("New awesome name")
      updated.password must beEqualTo("newTestPassword")
      updated.id must beSome(userId)
    }

    "not update user without id" in new WithRepository {
      // given
      Await.ready(userRepository.save(userA), 1.second)
      val toUpdate = userA.copy(name = "User without id", email = "newemail@example.com", password = "newTestPassword")

      // when
      Await.ready(userRepository.update(toUpdate), 1.second)

      // then
      val list = Await.result(userRepository.all, 1.second)
      list must have size(1)

      val updated = list.head
      updated.email must beEqualTo(userA.email)
      updated.name must beEqualTo(userA.name)
      updated.password must beEqualTo(userA.password)
    }

    "not update other users" in new WithRepository {
      // given
      Await.ready(userRepository.save(userA), 1.second)
      val toUpdate = userA.copy(name = "Nonexistent user", email = "nonexistent@example.com",
        password = "nonexistent@example.com", id = Some(Int.MaxValue))

      // when
      Await.ready(userRepository.update(toUpdate), 1.second)

      // then
      val list = Await.result(userRepository.all, 1.second)
      list must have size(1)

      val updated = list.head
      updated.email must beEqualTo(userA.email)
      updated.name must beEqualTo(userA.name)
      updated.password must beEqualTo(userA.password)
    }

  }

}
