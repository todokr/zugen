package services

import javax.inject.Inject

import domain.Id
import domain.customer.Customer
import domain.order.{Order, OrderRepository}
import services.OrderService.PlaceOrderCommand

class OrderService @Inject() (repository: OrderRepository) {

  /** An usecase of find an order */
  def findOrder(orderId: Id[Order]): Option[Order] = repository.resolve(orderId)

  /** An usecase of placing an order */
  def placeOrder(command: PlaceOrderCommand): Unit = {
    val placedOrder = Order.place(command.customerId)
    repository.store(placedOrder)
  }
}

object OrderService {

  /** A command to place an order */
  case class PlaceOrderCommand(customerId: Id[Customer])
}
