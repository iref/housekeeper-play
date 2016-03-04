package services

import repositories.UserRepository

trait ServiceComponents {

  def userRepository: UserRepository

  lazy val userService: UserService = new UserServiceImpl(userRepository)

}
