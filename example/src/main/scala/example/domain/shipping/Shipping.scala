package example.domain.shipping

import example.domain.order.Order

/**
  * 配送
  */
case class Shipping(id: ShippingId,
                    order: Order,
                    shippingAddress: ShippingAddress)

/**
  * 配送ID
  */
case class ShippingId(value: String) extends AnyVal

/**
  * 配送先アドレス
  */
case class ShippingAddress(value: String) extends AnyVal
