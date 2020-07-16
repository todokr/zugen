package services

import domain.Id
import domain.model.customer.Customer
import domain.repository.customer.CustomerRepository
import javax.inject.Inject
import services.CustomerService.RegisterCustomerCommand

class CustomerService @Inject() (repository: CustomerRepository) {

  /** An usecase of finding a customer */
  def findCustomer(customerId: Id[Customer]): Option[Customer] = repository.resolve(customerId)

  /** An usecase of registering a new customer */
  def registerCustomer(command: RegisterCustomerCommand): Unit = {
    val newCustomer = Customer.newCustomer(command.name)
    repository.store(newCustomer)
  }
}

object CustomerService {

  case class RegisterCustomerCommand(name: String)
}
