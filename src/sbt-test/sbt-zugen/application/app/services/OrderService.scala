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

class A {

  def methodA(x: Int): Int = {
    val b = new B
    b.methodB(x)
  }
}

class B {

  def methodB(x: Int): Int = {
    val d = new D
    C.methodC(x) + d.methodD(x)
  }
}

object C {

  def methodC(x: Int): Int = 1
}

class D extends E {

  def methodD(x: Int): Int = methodE(x)
}

trait E {

  def methodE(x: Int): Int = x + 1 + X(2).double()
}

case class X(value: Int) {

  def double(): Int = value * 2
}
