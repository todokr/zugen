package example.domain.shipping

import example.badreference.SomeClass
import example.domain.Id
import example.domain.order.Order

/**
  * 配送
  */
case class Shipping(id: Id[Shipping], order: Order, shippingAddress: ShippingAddress, someClass: SomeClass)

/**
  * 配送先アドレス
  */
case class ShippingAddress(value: String) extends AnyVal
