package repositories

import models.Household

import scala.concurrent.Future


trait HouseholdRepository {

  def find(id: Int): Future[Option[Household]]

  def save(household: Household): Future[Household]
}
