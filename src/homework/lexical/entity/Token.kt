package homework.lexical.entity

import homework.grammatical.entity.Token
import homework.lexical.entity.Category.*
import homework.lexical.utils.ConsoleColorRenderer.renderBlue
import homework.lexical.utils.ConsoleColorRenderer.renderCyan
import homework.lexical.utils.ConsoleColorRenderer.renderGreen
import homework.lexical.utils.ConsoleColorRenderer.renderMagenta
import homework.lexical.utils.ConsoleColorRenderer.renderRed
import homework.lexical.utils.ConsoleColorRenderer.renderYellow

/**
 * Token 的数据类
 * @property category Token 的种类
 * @property value Token 的值
 * @property line Token 所在的行号
 */
data class Token(
    val category: Category, // Token 的种类
    val value: String, // Token 的值
    val line: Int, // Token 所在的行号
) {
    // 输出 Token 的日志信息(用于调试)
    fun toLogStr(): String {
        return "Token(category='${
            when (category) {
                ERROR -> (ERROR.toString().renderRed())
                KEYWORD -> (KEYWORD.toString().renderYellow())
                CONSTANT, STRING, CHAR -> (category.toString().renderBlue())
                COMMENT -> (COMMENT.toString().renderMagenta())
                IDENTIFIER -> (IDENTIFIER.toString().renderGreen())
                OPERATOR -> (OPERATOR.toString().renderCyan())
                else -> category
            }
        }', value='${value.renderGreen()}', line=$line)"
    }

    // 输出 Token 的字符串信息(用于导出文件)
    fun toOutputStr(): String {
        val strCategory: String = tokenValueNum()
        return "($strCategory, \"$value\", $line)"
    }


    private fun tokenValueNum() = when (category) {
        KEYWORD -> {
            when (value) {
                "char" -> "101"
                "int" -> "102"
                "float" -> "103"
                "break" -> "104"
                "const" -> "105"
                "return" -> "106"
                "void" -> "107"
                "continue" -> "108"
                "do" -> "109"
                "while" -> "110"
                "if" -> "111"
                "else" -> "112"
                "for" -> "113"
                else -> "0"
            }
        }

        IDENTIFIER -> {
            "700"
        }

        OPERATOR -> {
            when (value) {
                "(" -> "201"
                ")" -> "202"
                "[" -> "203"
                "]" -> "204"
                "!" -> "205"
                "*" -> "206"
                "/" -> "207"
                "%" -> "208"
                "+" -> "209"
                "-" -> "210"
                "<" -> "211"
                "<=" -> "212"
                ">" -> "213"
                ">=" -> "214"
                "==" -> "215"
                "!=" -> "216"
                "&&" -> "217"
                "||" -> "218"
                "=" -> "219"
                "." -> "220"
                else -> "0"
            }
        }

        DELIMITER -> {
            when (value) {
                "{" -> "301"
                "}" -> "302"
                ";" -> "303"
                "," -> "304"
                else -> "0"
            }
        }

        CONSTANT -> {
            if (value.contains('.')) { // 浮点数
                "800"
            } else { // 整数
                "400"
            }
        }

        CHAR -> "500"
        STRING -> "600"
        COMMENT -> "0"
        ERROR -> "0"
        EOF -> "0"
    }


}


/** Token 的种类
 * @property KEYWORD 关键字
 * @property IDENTIFIER 标识符
 * @property OPERATOR 运算符
 * @property DELIMITER 分隔符
 * @property CONSTANT 常量
 * @property COMMENT 注释
 * @property ERROR 错误
 * @property EOF 文件结束
 *  */
enum class Category {
    // 关键字
    KEYWORD,

    // 标识符
    IDENTIFIER,

    // 运算符
    OPERATOR,

    // 分隔符
    DELIMITER,

    // 常量
    CONSTANT,

    // 字符
    CHAR,

    // 字符串
    STRING,

    // 注释
    COMMENT,

    // 错误
    ERROR,

    // 文件结束
    EOF
}

