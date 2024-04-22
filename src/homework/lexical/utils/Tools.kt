package homework.lexical.utils

// 判断字符是否是字母、数字或者下划线
fun Char.isLetterOrDigitOr_(): Boolean {
    return this.isLetterOrDigit() || this == '_'
}

// 判断字符是否是字母或者下划线
fun Char.isLetterOr_(): Boolean {
    return this.isLetter() || this == '_'
}

// 判断字符是否为空字符
fun Char.isBlankOrNewLine(): Boolean {
    return this.isWhitespace() || this == '\n' || this == '\r'
}

// 删除多行注释
fun removeMultipleComments(input: String): String {
    val regex = "/\\*.*?\\*/".toRegex()
    return input.replace(regex, "")
}

