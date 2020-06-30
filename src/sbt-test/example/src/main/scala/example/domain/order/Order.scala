package example.domain.order

import java.time.OffsetDateTime

import example.domain.product.Product
import example.domain.Id

/**
  * 注文
  * これは二行目
  */
case class Order(id: Id[Order], productId: Id[Product], orderedAt: OffsetDateTime)
