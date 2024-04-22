package homework.lexical

import com.alibaba.fastjson2.into
import com.alibaba.fastjson2.parseObject
import com.alibaba.fastjson2.toJSONString
import homework.lexical.entity.Category.*
import homework.lexical.entity.SampleWords
import homework.lexical.entity.Token
import homework.lexical.entity.TokenStore
import homework.lexical.recognizer.*
import homework.lexical.utils.ConsoleColorRenderer.renderGreen
import homework.lexical.utils.backIterator
import homework.lexical.utils.isBlankOrNewLine
import homework.lexical.utils.isLetterOr_
import homework.lexical.utils.removeMultipleComments


/**
 * 将字符串转换为 Token 列表
 * @param input 输入的字符串
 * @param isDebug 是否开启调试模式
 * @return Token 列表
 */
@JvmOverloads
fun codeStringToTokenList(input: String, isDebug: Boolean = false): List<Token> {
    // 初始化结果列表
    TokenStore.init()
    val result = TokenStore.tokens
    // 删除多行注释
    val changedInput = removeMultipleComments(input)
    // 将字符串按行分隔
    val lines = changedInput.split("\n")
    // 遍历每一行
    lines.forEachIndexed lineForEach@{ lineIndex, lineStrTemp ->
        // 消除当前行的空白
        val lineStr = lineStrTemp.trim()
        // 获取当前行的行号
        val line = lineIndex + 1

        if (isDebug) {// 如果是调试模式则输出当前行
            println("line=$line, lineStr=${lineStr.renderGreen()}")
        }

        // 如果当前行为空则跳过
        if (lineStrTemp.isBlank()) return@lineForEach

        // 如果该行是注释则跳过
        if (lineStr.startsWith("//")) {
            result.add(Token(COMMENT, lineStr, lineIndex))
            return@lineForEach
        }
        // 创建一个迭代器用于遍历每一行的字符
        val iterator = lineStr.backIterator()
        //
        charForeach@ while (iterator.hasNext()) {
            val char = iterator.next() // 获取当前字符
            iterator.back()
            when {
                char.isLetterOr_() -> idnRecognize(iterator, line) // 进入标识符或保留字识别
                char.isDigit() -> constantRecognizer(iterator, line) // 进入常量识别
                char.toString() in SampleWords.delimiters -> { // 分隔符识别
                    result.add(Token(DELIMITER, char.toString(), line))
                    iterator.next()
                }

                char == '"' -> {// 进入字符串识别
                    stringRecognizer(iterator, line)
                }

                char == '\'' -> {// 进入字符识别
                    charRecognizer(iterator, line)
                }

                char.toString() in SampleWords.operators || char == '&' -> operatorRecognizer(iterator, line) // 进入运算符识别
                char.isBlankOrNewLine() -> { // 如果是空白字符
                    iterator.next()
                    continue@charForeach
                }

                else -> { // 如果是其他字符
                    errorRecognize(iterator, line)
                }
            }
            if(!isDebug && result.last().category == ERROR) {
                val errorToken = result.last()
                throw Exception("Token检测错误：${errorToken.toLogStr()}")
            }
        }
    }
    return result
}

fun String.codeStrToTokenList(): List<Token> {
    return codeStringToTokenList(this)
}

// 一个测试案例
val testStr1 = """
        // 这是开头一个注释
        int main() {
            // 这是一个注释.
            int a = "y";
            int b = 2.6E16;
            int c = a + b; // 这是另一个注释
            a && b;
            c++;
            return c;
        }
    """.trimIndent()

// 另一个测试案例
val testStr2 = """
        int sum(int a, int b);
        
        main() {
            float a = 1.9;
            int b = 0;
            int c = a && b;
            int d, f
            return;
        }
        
        int sum(int a, int b) {
            return a + b;
        }
    """.trimIndent()

fun main() {
    TokenStore.init()
    println("str2测试")
    var tokens :List<Token> = codeStringToTokenList(testStr2)
    tokens.forEach {
        println(it.toLogStr())
    }
    val tJson = tokens.toJSONString()
    println(tJson)
/*
    val listType = tJson.into<List<Token>>()
    listType.forEach{
        println("it! $it")
    }
*/
}

