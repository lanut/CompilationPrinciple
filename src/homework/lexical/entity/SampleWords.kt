package homework.lexical.entity

/**
 * Sample语法的关键字、运算符和分隔符
 * @property keywords 关键字列表
 * @property operators 运算符列表
 * @property delimiters 分隔符列表
 */
object SampleWords {
    // Sample语法的关键字
    val keywords = listOf(
        "if", "else",
        "while", "for", "break", "continue", "do",
        "int", "float", "char",
        "void", "return", "const",
        "main"
    )

    // Sample语法的运算符
    val operators = listOf(
        "+", "-", "*", "/", "%",
        ">", "<", "=", "|",
        "^", ".", "!",

        "(", ")", "[", "]",

        ">=", "<=", "==", "!=", "&&",
        "||",

        // "&",

        // "+=", "-=", "*=", "/=", "%=", "++", "--"
    )

    // Sample语法的分隔符
    val delimiters = listOf(
        "{", "}", ",", ";"
    )

}