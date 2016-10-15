package repositories

import models.User
import test.HousekeeperSpec

class UserRepositorySpec extends HousekeeperSpec with Database {

  val userA = User("John Doe", "doe@example.com", "test_password")

  "UserRepository" should {

    "save new user" in {
      // when
      val Some(id) = userRepository.save(userA).futureValue.id

      // then
      val Some(found) = userRepository.find(id).futureValue
      found.id should be(Some(id))
      found.name should be(userA.name)
      found.email should be(userA.email)
      found.password should be(userA.password)
    }

    "return all users" in {
      // when
      userRepository.save(userA).futureValue

      val users = userRepository.all.futureValue
      users should have size (1)
    }

    "find existing user by id" in {
      // given
      val Some(userId) = userRepository.save(userA).futureValue.id

      // when
      val Some(found) = userRepository.find(userId).futureValue

      // then
      found.id should be(Some(userId))
      found.name should be(userA.name)
      found.email should be(userA.email)
      found.password should be(userA.password)
    }

    "find nonexistent user returns None" in {
      // given
      userRepository.save(userA).futureValue

      // when
      val notFound = userRepository.find(Int.MaxValue).futureValue

      // then
      notFound should be(None)
    }

    "find existing user by email" in {
      // given
      val Some(userId) = userRepository.save(userA).futureValue.id

      // when
      val Some(found) = userRepository.findByEmail(userA.email).futureValue

      // then
      found.id should be(Some(userId))
      found.name should be(userA.name)
      found.email should be(userA.email)
      found.password should be(userA.password)
    }

    "findByEmail returns None if user does not exist" in {
      // given
      userRepository.save(userA).futureValue

      // when
      val notFound = userRepository.findByEmail("nonexisting@email.com").futureValue

      // then
      notFound should be(None)
    }

    "update existing user" in {
      // given
      val Some(userId) = userRepository.save(userA).futureValue.id
      val toUpdate = userA.copy(id = Some(userId), name = "New awesome name", email = "new@example.com", password = "newTestPassword")

      // when
      userRepository.update(toUpdate).futureValue

      // then
      val Some(updated) = userRepository.find(userId).futureValue
      updated.email should be("new@example.com")
      updated.name should be("New awesome name")
      updated.password should be("newTestPassword")
      updated.id should be(Some(userId))
    }

    "not update user without id" in {
      // given
      userRepository.save(userA).futureValue
      val toUpdate = userA.copy(name = "User without id", email = "newemail@example.com", password = "newTestPassword")

      // when
      userRepository.update(toUpdate).futureValue

      // then
      val list = userRepository.all.futureValue
      list should have size (1)

      val updated = list.head
      updated.email should be(userA.email)
      updated.name should be(userA.name)
      updated.password should be(userA.password)
    }

    "not update other users" in {
      // given
      userRepository.save(userA).futureValue
      val toUpdate = userA.copy(name = "Nonexistent user", email = "nonexistent@example.com",
        password = "nonexistent@example.com", id = Some(Int.MaxValue))

      // when
      userRepository.update(toUpdate).futureValue

      // then
      val list = userRepository.all.futureValue
      list should have size (1)

      val updated = list.head
      updated.email should be(userA.email)
      updated.name should be(userA.name)
      updated.password should be(userA.password)
    }

  }

}
