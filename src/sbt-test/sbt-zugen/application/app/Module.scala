import com.google.inject.AbstractModule
import domain.repository.customer.CustomerRepository
import domain.repository.order.OrderRepository
import infrastructure.repository.customer.MockCustomerRepository
import infrastructure.repository.order.MockOrderRepository

class Module extends AbstractModule {

  override def configure(): Unit = {
    super.configure()

    bind(classOf[OrderRepository]).to(classOf[MockOrderRepository])
    bind(classOf[CustomerRepository]).to(classOf[MockCustomerRepository])
  }
}
