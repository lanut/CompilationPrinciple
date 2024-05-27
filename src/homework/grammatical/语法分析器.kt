package homework.grammatical

import com.alibaba.fastjson2.toJSONString
import homework.grammatical.entity.ExpressionStore
import homework.grammatical.recognizer.递归分析法
import homework.grammatical.utils.backIterator
import homework.grammatical.utils.jsonArrayToTokenList
import homework.grammatical.utils.outputSVGFile
import homework.grammatical.utils.toUmlTree
import homework.lexical.codeStrToTokenList


val testStr = """
//定义两个函数的测试，输入两个数，将其中较大的数加100输出，此函数需要进一步测试同名变量的使用
int a = 1 ;

int sum(int,int);
int max(int,int);
main(){

    int N = read() ;
    int M = read() ;
    a = sum(max(M,N),100) ;
    write(a);

}

int sum(int sum_x,int sum_y){

    int result ;
    result = sum_x + sum_y ;

    return result ;

}

int max(int m_x,int m_y){

    int result ;
    if (m_x >= m_y)result = m_x ;
	else result = m_y;
    return result ;

}
""".trimIndent()

val testExcp = "sum = sum +i"

fun 语法分析器(tokenJsStr: String): ExpressionStore {
    val tokenList = tokenJsStr.jsonArrayToTokenList()
    val backIterator = tokenList.backIterator()
    val 递归分析法 = 递归分析法()
    try {
        递归分析法.程序(backIterator)
    } catch (e: Exception) {
        // println(递归分析法.expressionStore)
        throw e
    }
    return 递归分析法.expressionStore
}

fun 表达式语法分析器(tokenJsStr: String): ExpressionStore {
    val tokenList = tokenJsStr.jsonArrayToTokenList()
    val backIterator = tokenList.backIterator()
    val 递归分析法 = 递归分析法()
    try {
        递归分析法.表达式(backIterator)
    } catch (e: Exception) {
        // println(递归分析法.expressionStore)
        throw e
    }
    return 递归分析法.expressionStore
}


fun main() {
    val lTokenList = testStr.codeStrToTokenList()
    println(lTokenList.joinToString(separator = "\n", postfix = "\n\n"))
    val jsonString = lTokenList.toJSONString()
    // println(jsonString)
    val expressionStore = 语法分析器(jsonString)
    println(expressionStore)
    val syntaxTree = expressionStore.toTree().apply { tidy() }
    val umlTree = syntaxTree.toUmlTree()
    println(umlTree)
    // println(syntaxTree.root.children[0])
    outputSVGFile(umlTree)
}