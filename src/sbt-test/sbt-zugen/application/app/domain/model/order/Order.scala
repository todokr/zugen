package domain.model.order

import domain.Id
import domain.model.customer.Customer
import strangepackage.WeirdClass

/** An order */
case class Order(
  orderId: Id[Order],
  customerId: Id[Customer],
  status: OrderStatus,
  something: WeirdClass
)

object Order {

  /** Place an order */
  def place(customerId: Id[Customer]): Order = Order(Id("1"), customerId, OrderStatus.Placed, WeirdClass())
}
