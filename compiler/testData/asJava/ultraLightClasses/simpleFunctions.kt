
class Foo {
  open fun bar(a: Int, b:Any, c:Foo): Unit {}
  internal fun bar2(a: Sequence, b: Unresolved) {}
  private fun bar3(x: Foo.Inner, vararg y: Inner) = "str"
  fun bar4() = 42

  operator fun plus(increment: Int): Foo {}
  fun String.onString(a: (Int) -> Any?): Foo {}

  class Inner {}
}
