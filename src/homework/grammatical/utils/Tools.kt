package homework.grammatical.utils

import homework.grammatical.entity.Category



// 匹配Token迭代器是否与字符串相等
fun match(backTokenIterator: BackTokenIterator, string: String): Boolean {
    val token = backTokenIterator.next()
    if (token.value == string) return true
    else {
        backTokenIterator.back()
        return false
    }
}

// 匹配Token迭代器是否与类别相等
fun match(backTokenIterator: BackTokenIterator, category: Category): Boolean {
    val token = backTokenIterator.next()
    if (token.category == category) return true
    else {
        backTokenIterator.back()
        return false
    }
}

