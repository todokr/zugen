package example.domain.literaltypes

/** Successful result */
case class Success(
  code: Code,
  retried: Int = 0,
  message: String = "nothing special"
)
