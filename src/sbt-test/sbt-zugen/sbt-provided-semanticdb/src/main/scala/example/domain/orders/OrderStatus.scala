package example.domain.orders

/** 注文ステータス */
sealed trait OrderStatus

/** 注文承り */
case object Placed extends OrderStatus

/** 注文確認済 */
case object Approved extends OrderStatus

/**  発送済 */
case object Delivered extends OrderStatus
