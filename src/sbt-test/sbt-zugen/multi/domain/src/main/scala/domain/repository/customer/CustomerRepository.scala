package domain.repository.customer

import domain.Id
import domain.model.customer.Customer

trait CustomerRepository {

  def resolve(customerId: Id[Customer]): Option[Customer]
  def store(customer: Customer): Unit
}
