package example.domain.product

/**
  * 商品
  */
case class Product(id: ProductId, name: ProductName, category: ProductCategory)

/**
  * 商品ID
  */
case class ProductId(value: String) extends AnyVal

/**
  * 商品名
  */
case class ProductName(value: String) extends AnyVal

/**
  * 商品カテゴリ
  */
sealed abstract class ProductCategory(code: String)

/**
  * 食品
  */
case object Food extends ProductCategory("FOOD")
case object Furniture extends ProductCategory("FURNITURE")
case class Hoge(value: Int) extends ProductCategory("HOGE")
