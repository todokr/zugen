package example.domain.misc

import example.badreference.SomeClass

/**
  * 普通のクラス
  */
class PlainClass {}

/**
  * 普通のトレイト
  */
trait PlainTrait {}

/**
  * 子のクラス
  */
class InheritClass extends PlainTrait

/**
  * 孫クラス
  */
class InheritClass2 extends InheritClass

/**
  * 複数のミックスイン
  */
class MixedClass extends PlainClass with PlainTrait

/**
  * プライベートだよ
  */
private class Himitsu

/**
  * パッケージプライベートだよ
  */
private[misc] class PackageHimitsu

/**
  * ドメイン外のパッケージを参照している悪いクラス
  */
case class HasBadReference(bad: SomeClass)
