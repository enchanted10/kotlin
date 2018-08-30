/** should load cls */
class MyList : List<String> {
  override operator fun get(index: Int): String {}
}

/** should load cls */
interface ASet<T> : MutableCollection<T> {}

/** should load cls */
abstract class MySet<T> : ASet<T> {
  override fun remove(elem: String): Boolean {}

}
