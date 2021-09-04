package example.domain.pets

/** ペットの販売ステータス */
sealed trait PetStatus

/** 販売中 */
case object Available extends PetStatus
/** 予約済 */
case object Reserved extends PetStatus
/** 売約済 */
case object Sold extends PetStatus
