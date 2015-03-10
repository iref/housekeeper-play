package repositories

/**
 * Database repositories module.
 */
trait Repositories {

  lazy val userRepository = new UserRepository

}
