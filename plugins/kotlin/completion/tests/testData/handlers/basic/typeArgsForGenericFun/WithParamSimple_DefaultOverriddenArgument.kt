fun <T: Number> create(param: List<T> = listOf(1 as T)): List<T> = TODO()
fun test() {
    val list: List<Int> = create(listOf(1)).<caret>
}

// ELEMENT: subList