package example.domain.users

import example.domain.Id

/**
  * サービスを利用できる人物
  */
case class User(
  id: Id[User],
  userName: UserName,
  email: String,
  role: Role
)

/**
  * ユーザー名
  */
case class UserName(firstName: FirstName, lastName: LastName)

/**
  * 名
  */
case class FirstName(value: String) extends AnyVal

/**
  * 姓
  */
case class LastName(value: String) extends AnyVal

/**
  * Eメールアドレス
  */
case class Email(value: String) extends AnyVal
