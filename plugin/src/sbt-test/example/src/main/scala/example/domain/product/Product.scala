package example.domain.product

import example.domain.Id

/**
  * 商品
  */
case class Product(id: Id[Product], name: ProductName, category: ProductCategory)

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

/**
  * 家具
  */
case object Furniture extends ProductCategory("FURNITURE")

/**
  * 電化製品
  */
case object ElectricAppliance extends ProductCategory("ELECTRIC_APPLIANCE")
