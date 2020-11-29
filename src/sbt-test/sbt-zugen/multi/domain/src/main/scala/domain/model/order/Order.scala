package domain.model.order

import domain.Id
import domain.model.customer.Customer

/** An order */
case class Order(
  orderId: Id[Order],
  customerId: Id[Customer],
  status: OrderStatus
)

object Order {

  /** Place an order */
  def place(customerId: Id[Customer]): Order = Order(Id("1"), customerId, OrderStatus.Placed)
}
