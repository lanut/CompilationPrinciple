package homework.grammatical.entity

import homework.grammatical.entity.Category.*

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
)


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