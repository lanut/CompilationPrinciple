package homework.grammatical

import com.alibaba.fastjson2.toJSONString
import homework.grammatical.entity.*
import homework.grammatical.recognizer.递归分析法
import homework.grammatical.utils.backIterator
import homework.grammatical.utils.jsonArrayToTokenList
import homework.grammatical.utils.outputSVGFile
import homework.lexical.codeStrToTokenList
import homework.grammatical.utils.toUmlTree


val testStr = """
    main() {
           int x = 2, y;
           int z = 3;
           write(x);
           write(z);
    }	
""".trimIndent()

fun 语法分析器(tokenJsStr: String): ExpressionStore {
    val tokenList = tokenJsStr.jsonArrayToTokenList()
    val backIterator = tokenList.backIterator()
    val 递归分析法 = 递归分析法()
    try {
        递归分析法.程序(backIterator)
    } catch (e: Exception) {
        println(递归分析法.expressionStore)
        throw e
    }
    return 递归分析法.expressionStore
}


fun main() {
    val lTokenList = testStr.codeStrToTokenList()
    val jsonString = lTokenList.toJSONString()
    println(jsonString)
    val expressionStore = 语法分析器(jsonString)
    println(expressionStore)
    val syntaxTree = expressionStore.toTree()
    val umlTree = syntaxTree.toUmlTree()
    // println(umlTree)
    outputSVGFile(umlTree)

}