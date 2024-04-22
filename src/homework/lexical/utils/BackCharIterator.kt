package homework.lexical.utils

/**
 * 可回溯字符迭代器接口
 */
abstract class BackCharIterator : CharIterator() {

    abstract fun back()

    abstract fun hasPrevious(): Boolean

}

// 为 CharSequence 添加一个扩展函数，返回一个 BackCharIterator
fun CharSequence.backIterator(): BackCharIterator = object : BackCharIterator() {
    private var index = 0

    override fun nextChar(): Char = get(index++)

    override fun hasNext(): Boolean = index < length

    override fun back() {
        if (index > 0) {
            index--
        }
    }
    override fun hasPrevious(): Boolean = index > 0

}

