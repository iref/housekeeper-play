package models

/**
 * Trait provides repositories implementations.
 */
trait Repositories {

  lazy val userRepositories = new ShoppingListRepository()

}
