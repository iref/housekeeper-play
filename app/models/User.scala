package models

case class User(name: String, email: String, password: String, id: Option[Int] = None)
