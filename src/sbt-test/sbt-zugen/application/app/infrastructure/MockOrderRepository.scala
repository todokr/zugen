package infrastructure

import scala.util.chaining._

import domain.Id
import domain.order.{Order, OrderRepository, OrderStatus}
import strangepackage.WeirdClass

class MockOrderRepository extends OrderRepository {

  /** resolve an order */
  override def resolve(orderId: Id[Order]): Option[Order] =
    Order(Id("1"), Id("100"), OrderStatus.Placed, WeirdClass()).pipe(Some(_))

  /** store an order */
  override def store(order: Order): Unit = ()
}
