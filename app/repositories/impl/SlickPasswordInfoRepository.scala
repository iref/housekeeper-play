package repositories.impl

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.util.BCryptPasswordHasher
import models.User
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import repositories.impl.tables.UsersTable
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

private [repositories] class SlickPasswordInfoRepository(protected val dbConfigProvider: DatabaseConfigProvider)
  extends DelegableAuthInfoDAO[PasswordInfo]
  with HasDatabaseConfigProvider[JdbcProfile]
  with UsersTable {

  import driver.api._

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val query = users.filter(u => u.email === loginInfo.providerKey).
        map(u => u.password)
    val action = query.result.
      headOption.
      map(passwordOption => passwordOption.map(p => PasswordInfo(BCryptPasswordHasher.ID, p)))

    db.run(action)
  }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val action = users.filter(u => u.email === loginInfo.providerKey).map(_.password).update(authInfo.password)
    db.run(action).map(_ => authInfo)
  }

  def remove(loginInfo: LoginInfo): Future[Unit] = {
    val action = users.filter(_.email === loginInfo.providerKey).delete
    db.run(action).map(_ => ())
  }

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val user = User("", loginInfo.providerKey, authInfo.password)
    db.run(users += user).map(_ => authInfo)
  }

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = update(loginInfo, authInfo)
}
