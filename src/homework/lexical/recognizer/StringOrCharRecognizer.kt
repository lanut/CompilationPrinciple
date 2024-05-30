package homework.lexical.recognizer

import homework.lexical.entity.Category
import homework.lexical.entity.Token
import homework.lexical.utils.BackCharIterator
import homework.lexical.utils.isBlankOrNewLine

// 用于识别字符串
fun stringRecognizer(charIterator: BackCharIterator, line: Int, tokens:MutableList<Token>) {
    val stringBuffer = StringBuffer()
    var char = charIterator.nextChar()
    stringBuffer.append(char) // 合并第一个双引号
    // 循环查找字符串，并且可以判断转义字符,并可以区分分隔符
    while (charIterator.hasNext()) {
        char = charIterator.nextChar()
        stringBuffer.append(char)
        if (char == '"') {
            break
        } else if (char == '\\') {
            char = charIterator.nextChar()
            stringBuffer.append(char)
        }
    }
    val string = stringBuffer.toString()
    if (string.isEmpty() || string.last() != '"') {
        tokens.add(Token(Category.ERROR, string, line))
        return
    } else if (string.endsWith("\\\"")) {
        tokens.add(Token(Category.ERROR, string, line))
        return
    }
    tokens.add(Token(Category.STRING, string, line))
}

// 用于识别字符
fun charRecognizer(charIterator: BackCharIterator, line: Int, tokens:MutableList<Token>) {
    val stringBuffer = StringBuffer()
    var char = charIterator.nextChar()
    stringBuffer.append(char) // 合并第一个单引号
    try {
        char = charIterator.nextChar()
    } catch (e: StringIndexOutOfBoundsException) {
        tokens.add(Token(Category.ERROR, stringBuffer.toString(), line))
        return
    }
    if (char == '\\') {
        stringBuffer.append(char)
        try {
            char = charIterator.nextChar()
            stringBuffer.append(char)
            char = charIterator.nextChar()
            stringBuffer.append(char)
        } catch (e: StringIndexOutOfBoundsException) {
            tokens.add(Token(Category.ERROR, stringBuffer.toString(), line))
            return
        }
    } else {
        stringBuffer.append(char)
        char = charIterator.nextChar()
        stringBuffer.append(char)
    }
    val string = stringBuffer.toString()
    if (string.endsWith("'")) {
        tokens.add(Token(Category.CHAR, string, line))
    } else {
        // 直到遍历完或遍历到空白字符
        while (charIterator.hasNext() && !char.isBlankOrNewLine() && char != '\'') {
            char = charIterator.nextChar()
            stringBuffer.append(char)
        }
        tokens.add(Token(Category.ERROR, stringBuffer.toString(), line))
    }
}