package example.domain.order

import example.domain.product.ProductId

/**
  * 注文
  * これは二行目
  */
case class Order(id: OrderId, productId: ProductId)

/**
  * 注文ID
  */
case class OrderId(value: String) extends AnyVal
