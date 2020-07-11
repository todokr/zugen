package domain.order

sealed trait OrderStatus

/** Status of an order */
object OrderStatus {

  /** Placed Status */
  case object Placed extends OrderStatus

  /** Completed Status */
  case object Completed extends OrderStatus
}
