package repositories.impl

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.db.slick.HasDatabaseConfig
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import models.{ShoppingList, ShoppingListDetail}
import repositories.ShoppingListRepository
import repositories.impl.tables.{ShoppingListItemsTable, ShoppingListTable}

private[repositories] class SlickShoppingListRepository(protected val dbConfig: DatabaseConfig[JdbcProfile])
    extends HasDatabaseConfig[JdbcProfile] with ShoppingListRepository
    with ShoppingListTable with ShoppingListItemsTable {

  import driver.api._

  def all: Future[List[ShoppingList]] = db.run(shoppingLists.result.map(_.toList))

  def find(id: Int): Future[Option[ShoppingListDetail]] = {
    val leftJoin = for {
      (l, i) <- shoppingLists joinLeft shoppingListItems on (_.id === _.shoppingListId) if l.id === id
    } yield (l, i)

    db.run(leftJoin.result).map { result =>
      result.groupBy(_._1).map {
        case (list, rows) => ShoppingListDetail(list, rows.map(_._2).flatMap(_.toList))
      }.headOption
    }
  }

  def save(shoppingList: ShoppingList): Future[ShoppingList] = {
    val insertWithId = (shoppingLists returning shoppingLists.map(_.id))
      .into((_, id) => shoppingList.copy(id = id))
    val query = insertWithId += shoppingList
    db.run(query.transactionally)
  }

  def remove(id: Int): Future[Int] = {
    val query = shoppingLists.filter(_.id === id).delete
    db.run(query.transactionally)
  }

  def update(shoppingList: ShoppingList): Future[Int] = {
    val query = shoppingLists.filter(_.id === shoppingList.id).update(shoppingList)
    db.run(query.transactionally)
  }

}
