package domain.order

import domain.Id

trait OrderRepository {

  /** resolve an order */
  def resolve(orderId: Id[Order]): Option[Order]

  /** store an order */
  def store(order: Order): Unit
}
