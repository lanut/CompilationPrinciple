package homework.lexical.recognizer

import homework.lexical.entity.Category.*
import homework.lexical.entity.SampleWords
import homework.lexical.entity.Token
import homework.lexical.entity.TokenStore.tokens
import homework.lexical.utils.BackCharIterator

// operatorRecognizer 函数用于识别运算符
fun operatorRecognizer(charIterator: BackCharIterator, line: Int) {
    // 读取字符
    val char = charIterator.nextChar()
    // stringBuffer 用于存储识别的字符
    val sb = StringBuffer()
    sb.append(char)
    if (charIterator.hasNext()) {
        sb.append(charIterator.nextChar())
    }
    // 如果 stringBuffer 中的字符串是运算符，则将其添加到 tokens 中，类别为 OPERATOR
    if (sb.toString() in SampleWords.operators) {
        tokens.add(Token(OPERATOR, sb.toString(), line))
    }
    // 如果 stringBuffer 中的字符串是 "//"，则将其视为注释，将其添加到 tokens 中，类别为 COMMENT
    else if (sb.toString() == "//") {
        while (charIterator.hasNext()) {
            sb.append(charIterator.next())
        }
        tokens.add(Token(COMMENT, sb.toString(), line))
    } else if (char.toString() in SampleWords.operators) {
        tokens.add(Token(OPERATOR, char.toString(), line))
        // 将字符迭代器回退一个字符
        charIterator.back()
    } else { // 否则，将 char 添加到 tokens 中，类别为 OPERATOR
        tokens.add(Token(ERROR, char.toString(), line))
        charIterator.back()

    }
}