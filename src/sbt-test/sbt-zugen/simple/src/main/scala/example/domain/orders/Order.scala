package example.domain.orders

import java.time.Instant

import example.badreference.SomeClass
import example.domain.Id
import example.domain.pets.Pet
import example.domain.users.User

/**
  * ペット購入の注文
  */
case class Order(
  id: Id[Order],
  petId: Id[Pet],
  userId: Id[User],
  shipDate: Option[Instant] = None,
  status: OrderStatus = Placed,
  complete: Boolean = false,
  someClass: SomeClass
)
