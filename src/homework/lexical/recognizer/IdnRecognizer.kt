package homework.lexical.recognizer

import homework.lexical.entity.Category
import homework.lexical.entity.SampleWords
import homework.lexical.entity.Token
import homework.lexical.recognizer.IdnRecognizer.State.*
import homework.lexical.utils.BackCharIterator
import homework.lexical.utils.isLetterOrDigitOr_
import homework.lexical.utils.isLetterOr_

// IDN_Recognizer 类用于识别标识符
class IdnRecognizer {
    // state 表示当前的状态
    var state = START
    // buffer 用于存储识别的字符
    val buffer = StringBuilder()

    // State 枚举定义了识别过程中可能的状态
    enum class State {
        START, IDN, ERROR
    }
}

// idnRecognize 函数用于识别标识符
fun idnRecognize(charIterator: BackCharIterator, line: Int, tokens:MutableList<Token>) {
    val idnRecognizer = IdnRecognizer()
    // 循环读取字符，直到识别出错误或者读取完所有字符
    while (charIterator.hasNext() && idnRecognizer.state != ERROR) {
        val char = charIterator.next()
        when (idnRecognizer.state) {
            // 如果当前状态为 START，且字符为字母或下划线，则转换状态为 IDN，并将字符添加到 buffer 中
            START -> {
                if (char.isLetterOr_()) {
                    idnRecognizer.state = IDN
                    idnRecognizer.buffer.append(char)
                } else {
                    idnRecognizer.state = ERROR
                }
            }
            // 如果当前状态为 IDN，且字符为字母、数字或下划线，则将字符添加到 buffer 中
            IDN -> {
                if (char.isLetterOrDigitOr_()) {
                    idnRecognizer.buffer.append(char)
                } else {
                    idnRecognizer.state = ERROR
                }
            }
            // 如果当前状态为 ERROR，则不做任何操作
            ERROR -> Unit
        }
    }
    // 从 buffer 中获取识别的结果
    val result = idnRecognizer.buffer.toString()
    // 如果结果是关键字，则添加到 tokens 中，类别为 KEYWORD
    if (result in SampleWords.keywords) {
        tokens.add(Token(Category.KEYWORD, result, line))
    } else {
        // 否则，添加到 tokens 中，类别为 IDENTIFIER
        tokens.add(Token(Category.IDENTIFIER, result, line))
    }
    // 如果当前状态为 ERROR，则将字符迭代器回退一个字符
    if (idnRecognizer.state == ERROR) {
        charIterator.back()
    }
}