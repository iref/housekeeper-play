package repositories

import models.{User, Household}
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

class HouseholdRepositorySpec extends Specification {

  val user = User("Test user", "user@gmail.com", "password")
  val household = Household("Testing household", Some("Really awesome household"), Some("http://awesomelogo.com"))

  "#find" should {

    "find existing household by id" in new Database {
      // given
      val Some(userId) = Await.result(userRepository.save(user), 1.second).id
      val household = Household("Testing household", Some("Really awesome household"), Some("http://awesomelogo.com"), Some(userId))
      val Some(id) = Await.result(householdRepository.save(household), 1.second).id

      // when
      val Some(foundHousehold) = Await.result(householdRepository.find(id), 1.second)

      // then
      foundHousehold.name must beEqualTo(household.name)
      foundHousehold.description must beEqualTo(household.description)
      foundHousehold.ownerId must beEqualTo(household.ownerId)
      foundHousehold.logo must beEqualTo(household.logo)
    }

    "find nonexistent household by id" in new Database {
      // when
      val notFound = Await.result(householdRepository.find(1), 1.second)

      // then
      notFound must beNone
    }

  }

  "#save" should {

    "return id of saved household" in new Database {
      // given
      val userId = Await.result(userRepository.save(user), 1.second).id
      val toSave = household.copy(ownerId = userId)

      // when
      val Some(householdId) = Await.result(householdRepository.save(toSave), 1.second).id

      // then
      val Some(stored) = Await.result(householdRepository.find(householdId), 1.second)
      stored.id must beSome(householdId)
      stored.name must beEqualTo(toSave.name)
      stored.description must beEqualTo(toSave.description)
      stored.logo must beEqualTo(toSave.logo)
      stored.ownerId must beEqualTo(toSave.ownerId)
    }

  }

}
