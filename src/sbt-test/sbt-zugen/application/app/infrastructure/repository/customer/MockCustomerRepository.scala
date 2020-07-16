package infrastructure.repository.customer

import scala.util.chaining._

import domain.Id
import domain.model.customer.{Customer, CustomerName}
import domain.repository.customer.CustomerRepository

class MockCustomerRepository extends CustomerRepository {
  override def resolve(customerId: Id[Customer]): Option[Customer] =
    Customer(Id("100"), CustomerName("A customer")).pipe(Some(_))
  override def store(customer: Customer): Unit = ()
}
