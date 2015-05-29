package models

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class ShoppingList(title: String, description: Option[String] = None, id: Option[Int] = None)

case class ShoppingListDetail(shoppingList: ShoppingList, items: Seq[ShoppingListItem])

trait ShoppingListRepository { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class ShoppingListsTable(tag: Tag) extends Table[ShoppingList](tag, "shopping_lists") {
    def title = column[String]("title", ColumnOption.NotNull)
    def description = column[Option[String]]("description", ColumnOption.Nullable)
    def id = column[Option[Int]]("id", slick.ast.ColumnOption.PrimaryKey, slick.ast.ColumnOption.AutoInc)

    def * = (title, description, id) <> ((ShoppingList.apply _).tupled, ShoppingList.unapply)
  }

}

class ShoppingListRepositoryImpl(protected val dbConfigProvider: DatabaseConfigProvider) 
  extends HasDatabaseConfigProvider[JdbcProfile] with ShoppingListRepository  with ShoppingListItemRepository {
  
  import driver.api._

  private val shoppingLists = TableQuery[ShoppingListsTable]
  private val shoppingListItems = TableQuery[ShoppingListItemTable]

  def all: Future[Seq[ShoppingList]] = db.run(shoppingLists.result)

  def find(id: Int): Future[Option[ShoppingListDetail]] = {
    val q = (for {
      lists <- shoppingLists.filter(_.id === id).take(1)
      items <- shoppingListItems if items.shoppingListId === lists.id
    } yield (lists, items))

    db.run(q.result).map { result =>
      result.groupBy(_._1).map {
        case (list, rows) => ShoppingListDetail(list, rows.map(_._2))
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
