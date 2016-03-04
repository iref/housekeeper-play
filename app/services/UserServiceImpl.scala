package services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import repositories.UserRepository

class UserServiceImpl(userRepository: UserRepository) extends UserService {

  def save(user: User): Future[User] =
    user.id match {
      case Some(_) => userRepository.update(user).map(_ => user)
      case None => userRepository.save(user)
    }

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userRepository.findByEmail(loginInfo.providerKey)

  def find(id: Int): Future[Option[User]] = userRepository.find(id)

}
