import com.google.inject.AbstractModule
import domain.order.OrderRepository
import infrastructure.MockOrderRepository

class Module extends AbstractModule {

  override def configure(): Unit = {
    super.configure()

    bind(classOf[OrderRepository]).to(classOf[MockOrderRepository])
  }
}
