package homework.lexical.recognizer

import homework.lexical.entity.Category.ERROR
import homework.lexical.entity.SampleWords
import homework.lexical.entity.Token
import homework.lexical.entity.TokenStore.tokens
import homework.lexical.utils.BackCharIterator
import homework.lexical.utils.isBlankOrNewLine

// errorRecognize 函数用于识别错误
fun errorRecognize(charIterator: BackCharIterator, line: Int) {
    // stringBuffer 用于存储识别的字符
    val stringBuffer = StringBuffer()
    var char = charIterator.next()
    // 循环读取字符，直到识别出错误或者读取完所有字符,或识别出界符
    while (charIterator.hasNext() && !char.isBlankOrNewLine() && (char.toString() !in SampleWords.delimiters)) {
        stringBuffer.append(char)
        char = charIterator.next()
    }
    if (char.toString() in SampleWords.delimiters) {
        charIterator.back()
    }
    // 将识别的结果添加到 tokens 中，类别为 ERROR
    tokens.add(Token(ERROR, stringBuffer.toString(), line))
}