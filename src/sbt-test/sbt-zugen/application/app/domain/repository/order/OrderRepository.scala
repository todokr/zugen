package domain.repository.order

import domain.Id
import domain.model.order.Order

trait OrderRepository {

  /** resolve an order */
  def resolve(orderId: Id[Order]): Option[Order]

  /** store an order */
  def store(order: Order): Unit
}
