package example.domain.users

final case class Role(roleRepr: String)

/**
  * ユーザーのロール
  */
object Role {
  val Customer: Role = Role("Customer")
  val Agent: Role = Role("Agent")
}
