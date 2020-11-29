package services

class ComplicatedService {

  def calcAnswer(x: Int): Int = {
    val b = new ClassB
    b.methodB(x)
  }
}

class ClassA {

  def methodA(x: Int): Int = x * 10
}

class ClassB {

  def methodB(x: Int): Int = {
    val d = new D
    C.methodC(x) + d.methodD(x)
  }
}

object C {

  def methodC(x: Int): Int = x
}

class D extends E {

  def methodD(x: Int): Int = methodE(x)
}

trait E {

  def methodE(x: Int): Int = x + 1 + X(2).double()
}

case class X(value: Int) {

  def double(): Int = value * 2
}
