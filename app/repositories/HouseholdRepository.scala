package repositories

import scala.concurrent.Future

import models.Household


trait HouseholdRepository {

  def find(id: Int): Future[Option[Household]]

  def save(household: Household): Future[Household]
}
