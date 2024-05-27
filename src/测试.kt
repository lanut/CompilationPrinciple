import com.alibaba.fastjson2.toJSONString
import homework.grammatical.recognizer.递归分析法
import homework.grammatical.utils.backIterator
import homework.grammatical.utils.jsonArrayToTokenList
import homework.grammatical.utils.outputSVGFile
import homework.grammatical.utils.toUmlTree
import homework.lexical.codeStringToTokenList
import homework.quaternionTranslators.QuaternionGeneration


val testExcp = "x = x+6/5>0 || 2 && 0 * (2 + 3 + 5) * 2 * 3 * 3 * 3 * fun(x, 3 + y, 2)"
val testExcp1 = "x = 2 * fun(y+6, 9) * 9 + 6 && 7 || 2 * (1 + 6)"
val testExcp2 = "fun(y+6, 9)"
val testExcp3 = "f(n - 1)+ f(n - 2)+ f(n - 3);"
val testExcp4 = "1 + 2 + 3"
val testMain = """
int f(int);
main()
{
   int m;
   m = read();
   write(f(m));

}

int f(int n) {
   int a;
   if(n==1) {
		return 1;
	}
   else if(n==2) {
		return 2;
	}
   else if(n==3) {
		return 4;
	}
    a = f(n - 1)+ f(n - 2)+ f(n - 3);
    return a ;
 }
""".trimIndent()

fun main() {
    // testMain(testMain)
    // testExcp(testExcp3)
    customTest()
}

fun testMain(testStr: String) {
    val tokenJsStr = codeStringToTokenList(testMain).toJSONString()
    // val tokenJsStr = codeStringToTokenList(testExcp).toJSONString()
    val tokenList = tokenJsStr.jsonArrayToTokenList()
    val backIterator = tokenList.backIterator()
    val 递归分析法 = 递归分析法()
    递归分析法.程序(backIterator)
    // 递归分析法.表达式(backIterator)
    val expressionStore = 递归分析法.expressionStore
    val syntaxTree = expressionStore.toTree().apply {
         this.tidy()
    }
    val umlTree = syntaxTree.toUmlTree()
    println(umlTree)
    outputSVGFile(umlTree, "test.svg")


}

fun testExcp(excp:String) {
    val tokenJsStr = codeStringToTokenList(excp).toJSONString()
    val tokenList = tokenJsStr.jsonArrayToTokenList()
    val backIterator = tokenList.backIterator()
    val 递归分析法 = 递归分析法()
    递归分析法.表达式(backIterator)
    val expressionStore = 递归分析法.expressionStore
    val syntaxTree = expressionStore.toTree().apply { tidy() }
    val umlTree = syntaxTree.toUmlTree()
    println(umlTree)
    println()
    println("四元式：$excp")
    val infiniteSequence = generateSequence(1) { it + 1 }
    val index = infiniteSequence.iterator()
    outputSVGFile(umlTree, "testExcp.svg")
    QuaternionGeneration().表达式(index, syntaxTree.root).forEach {
        println(it)
    }
}


fun customTest() {
    val customTest = """
//函数调用，空函数
void f();

main(){
  f();
}	
void f(){}

""".trimIndent()
    val tokenJsStr = codeStringToTokenList(customTest).toJSONString()
    val tokenList = tokenJsStr.jsonArrayToTokenList()
    val backIterator = tokenList.backIterator()
    val 递归分析法 = 递归分析法()
    递归分析法.程序(backIterator) // TODO 此处为测试的入口
    val expressionStore = 递归分析法.expressionStore
    val syntaxTree = expressionStore.toTree().apply { tidy() }
    val umlTree = syntaxTree.toUmlTree()
    println(umlTree)
    outputSVGFile(umlTree, "自定义测试.svg")
    println()
    println("四元式：$customTest")
    outputSVGFile(umlTree, "自定义测试.svg")
    val quaternionGeneration = QuaternionGeneration()
    quaternionGeneration.程序(syntaxTree.root) // TODO 此处为测试的入口
    quaternionGeneration.qExpressionList.forEach {
        println(it)
    }
    println("函数表")
    quaternionGeneration.functionList.forEach { function ->
        println(function)
        println("函数体")
        function.qExpressionList.forEach { qExpression ->
            println(qExpression)
        }
    }
}