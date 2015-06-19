package models

case class Household(name: String, description: Option[String],
                     logo: Option[String], ownerId: Option[Int] = None,
                     id: Option[Int] = None)
