package homework.grammatical.utils

import homework.grammatical.entity.Token

abstract class BackTokenIterator : ListIterator<Token> {

    abstract fun back()


}

fun List<Token>.backIterator(): BackTokenIterator = object : BackTokenIterator() {
    private var index = 0

    override fun back() {
        if (index > 0) {
            index--
        }
    }

    override fun hasPrevious(): Boolean = index > 0

    override fun hasNext(): Boolean = index < size

    override fun next(): Token = get(index++)

    override fun nextIndex(): Int = index + 1

    override fun previous(): Token = get(--index)

    override fun previousIndex(): Int = index - 1

}