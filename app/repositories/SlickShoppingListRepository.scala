package repositories

import models.{ShoppingList, ShoppingListDetail, ShoppingListItemsTable, ShoppingListTable}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global // TODO use actual execution context here
import scala.concurrent.Future

private[repositories] class SlickShoppingListRepository(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with ShoppingListRepository
  with ShoppingListTable with ShoppingListItemsTable {

  import driver.api._

  private val shoppingLists = TableQuery[ShoppingLists]
  private val shoppingListItems = TableQuery[ShoppingListItems]

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
