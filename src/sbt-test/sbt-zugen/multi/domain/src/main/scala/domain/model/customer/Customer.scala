package domain.model.customer

import domain.Id

/** A customer */
case class Customer(id: Id[Customer], name: CustomerName)

object Customer {

  /** Create new customer */
  def newCustomer(name: String): Customer = Customer(Id("999"), CustomerName(name))
}
