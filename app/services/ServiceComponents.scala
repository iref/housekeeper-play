package services

import com.mohiva.play.silhouette.api.util.Clock
import play.api.Configuration
import repositories.UserRepository

trait ServiceComponents {

  def userRepository: UserRepository

  def clock: Clock

  def configuration: Configuration

  lazy val userService: UserService = new UserServiceImpl(userRepository)

  lazy val rememberMeService: RememberMeService = new RememberMeServiceImpl(clock, configuration)

}
