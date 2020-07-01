package example.domain.orders

/**
  * 注文ステータス
  */
sealed trait OrderStatus
case object Approved extends OrderStatus
case object Delivered extends OrderStatus
case object Placed extends OrderStatus
