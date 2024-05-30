package homework.interpreter

import com.alibaba.fastjson2.toJSONString
import homework.grammatical.utils.outputSVGFile
import homework.grammatical.utils.toUmlTree
import homework.grammatical.语法分析器
import homework.lexical.codeStringToTokenList
import homework.quaternionTranslators.QuaternionGeneration
import homework.quaternionTranslators.entity.QExpression

// 变量类
data class Variable(val name: String, var value: String, val namespace: String = "global")

/**
 * 状态类
 * @param stateName 状态名
 * @param qExpressionList 四元式列表

 */
class Statement(val stateName: String, val qExpressionList: List<QExpression>) {
    // 变量表
    val variableStore = mutableListOf<Variable>()

    // 临时变量表
    val tempVariableStore = mutableListOf<Variable>()

    // 执行索引
    var executeIndex = 0

    // 返回值
    var returnValue = ""

    // 命名空间列表
    val namespaceList = mutableListOf("global")

    // 添加到变量表
    fun addVariable(variable: Variable) {
        // 如果变量名为 '&' 开头，则为临时变量
        if (variable.name.startsWith('&')) {
            tempVariableStore.add(variable)
        } else {
            variableStore.add(variable)
        }
    }


    // 根据变量名获取值
    fun getValueByName(name: String): String {
        // 如果为临时变量，则获取之后删除此变量
        if (name.startsWith('&')) {
            val tempVariable = tempVariableStore.find { it.name == name }
            tempVariableStore.remove(tempVariable)
            return tempVariable?.value ?: ""
        } else {
            return variableStore.find { it.name == name }?.value ?: ""
        }
    }

    // 根据变量名获取变量
    fun getVariableByName(name: String): Variable? {
        // 如果为临时变量，且没有找到，则创建一个临时变量
        return if (name.startsWith('&')) {
            tempVariableStore.find { it.name == name } ?: Variable(name, "").apply {
                tempVariableStore.add(this)
            }
        } else {
            variableStore.find { it.name == name }
        }
    }
}

/**
 * 解释器类
 * @param quaternionGeneration 四元式生成器
 * @param input 输入

 */
class Interpreter(quaternionGeneration: QuaternionGeneration, input: String) {
    // 函数表
    val functionStore = quaternionGeneration.functionList

    // 全局变量表
    val globalVariableStore = mutableListOf<Variable>()

    // 是否为定义变量状态
    var isAllocState = true

    //状态表
    val stateStore = mutableListOf<Statement>()

    // 参数申请栈
    val parameterStack = mutableListOf<String>()

    // 程序是否结束
    var isEnd = false

    // 输入重定向
    var inputDirection = input.let {
        // 如果输入为空字符串或只包含空白，则返回空列表
        if (input.isBlank()) {
            emptyList<String>().toMutableList()
        } else {
            input.split("\\s+".toRegex()).toMutableList()
        }
    }

    // 输出字符串
    var outputString = ""

    init {
        // 创建一个初始状态
        val initialState = Statement("global", quaternionGeneration.qExpressionList)
        stateStore.add(initialState)
    }

    // 根据变量名获取值
    fun getValueByName(name: String): String {
        val statement = stateStore.last()
        // 获取变量值
        val value = statement.getValueByName(name)
        // 如果值不为空，则返回值
        if (value != "") return value
        // 否则从全局变量表查找
        return globalVariableStore.find { it.name == name }?.value ?: ""
    }

    // 根据变量名获取变量
    fun getVariableByName(name: String): Variable {
        val statement = stateStore.last()
        // 获取变量
        val variable = statement.getVariableByName(name)
        // 如果变量不为空，则返回变量
        if (variable != null) return variable
        // 否则从全局变量表查找
        return globalVariableStore.find { it.name == name } ?: throw Exception("变量 $name 未定义")
    }

    // 智能根据value判断返回内容
    fun getAutoValueByName(value: String): String {
        // 如果value为常量，则直接返回
        if (value.isConstant()) {
            return value
        }
        // 否则变量名获取值
        return getValueByName(value)
    }

    // 执行单步函数
    fun executeStep() {
        // 获取最新状态
        val state = stateStore.lastOrNull()
        if (state == null) {
            isEnd = true
            return
        }
        // 获取变量表
        val variableStore = state.variableStore
        // 获取临时变量表
        val tempVariableStore = state.tempVariableStore
        // 当前执行的四元式索引
        state.executeIndex
        // 获取当前执行的四元式列表
        val qExpressionList = state.qExpressionList
        // 获取当前执行的四元式
        val qExpression = qExpressionList.getOrNull(state.executeIndex)
        if (qExpression == null) {
            // 退出状态栈
            stateStore.removeLast()
            return
        }
        // 执行四元式
        when (qExpression.instruction) {
            "init" -> {
                state.executeIndex++
            }
            // 变量声明
            "alloc" -> {
                // 获取变量名
                val variableName = qExpression.result
                // 如果是全局变量声明状态
                if (isAllocState) {
                    // 添加到全局变量表
                    globalVariableStore.add(Variable(name = variableName, value = "0"))
                } else {
                    // 添加到变量表
                    variableStore.add(
                        Variable(
                            name = variableName, value = "0", namespace = state.namespaceList.last()
                        )
                    )
                }
                state.executeIndex++
            }

            // 赋值
            "::=" -> {
                // 获取变量名
                val variableName = qExpression.result
                // 获取值
                val realValue = getAutoValueByName(qExpression.value1)
                // 获取变量
                val variable = getVariableByName(variableName)
                // 赋值
                variable.value = realValue
                state.executeIndex++
            }

            // 运算符
            "+", "-", "*", "/", "%", ">", "<", "!", ">=", "<=", "==", "!=", "&&", "||" -> {
                // 获取操作数1
                val value1 = qExpression.value1
                // 获取操作数2
                val value2 = qExpression.value2
                // 获取结果
                val result = qExpression.result
                // 获取操作数1的值
                val realValue1 = getAutoValueByName(value1)
                // 获取操作数2的值
                val realValue2 = getAutoValueByName(value2)
                // 计算结果
                val realResult = when (qExpression.instruction) {
                    "+" -> realValue1.toInt() + realValue2.toInt()
                    // 如果value2 为空字符串，则为负号
                    "-" -> {
                        if (realValue2 == "") {
                            -realValue1.toInt()
                        } else {
                            realValue1.toInt() - realValue2.toInt()
                        }
                    }

                    "*" -> realValue1.toInt() * realValue2.toInt()
                    "/" -> realValue1.toInt() / realValue2.toInt()
                    "%" -> realValue1.toInt() % realValue2.toInt()
                    ">" -> if (realValue1.toInt() > realValue2.toInt()) 1 else 0
                    "<" -> if (realValue1.toInt() < realValue2.toInt()) 1 else 0
                    "!" -> if (realValue1.toInt() == 0) 1 else 0
                    ">=" -> if (realValue1.toInt() >= realValue2.toInt()) 1 else 0
                    "<=" -> if (realValue1.toInt() <= realValue2.toInt()) 1 else 0
                    "==" -> if (realValue1.toInt() == realValue2.toInt()) 1 else 0
                    "!=" -> if (realValue1.toInt() != realValue2.toInt()) 1 else 0
                    "&&" -> if (realValue1.toInt() != 0 && realValue2.toInt() != 0) 1 else 0
                    "||" -> if (realValue1.toInt() != 0 || realValue2.toInt() != 0) 1 else 0
                    else -> throw Exception("未知的运算符")
                }
                // 获取结果变量
                val variable = getVariableByName(result)
                // 赋值
                variable.value = realResult.toString()
                state.executeIndex++
            }

            // 创建命名空间
            "block" -> {
                // 创建一个命名空间
                val namespace = "namespace${state.namespaceList.size}"
                // 添加到命名空间列表
                state.namespaceList.add(namespace)
                state.executeIndex++
            }

            // 删除命名空间
            "end" -> {
                // 删除此命名空间里的变量
                state.variableStore.removeAll { it.namespace == state.namespaceList.last() }
                // 删除最近的命名空间
                state.namespaceList.removeLast()
                state.executeIndex++
            }

            // 跳转
            "j" -> {
                // 获取跳转的四元式索引
                val jumpIndex = qExpression.result.toInt()
                // 跳转
                state.executeIndex = jumpIndex
            }

            // 条件跳转
            "jnz" -> {
                val value = getAutoValueByName(qExpression.value1)
                if (value == "1") {
                    // 获取跳转的四元式索引
                    val jumpIndex = qExpression.result.toInt()
                    // 跳转
                    state.executeIndex = jumpIndex
                } else {
                    state.executeIndex++
                }
            }

            // 获取参数
            "param" -> {
                // 添加到参数栈
                val result = if (qExpression.value1.isConstant()) {
                    qExpression.value1
                } else {
                    // 否则为变量名
                    getValueByName(qExpression.value1)
                }
                parameterStack.add(result)
                state.executeIndex++
            }

            // 函数调用
            "call" -> {
                // 获取函数名
                val functionName = qExpression.value1
                when (functionName) {
                    "write" -> { // 输出函数
                        val value = parameterStack.getOrNull(0)
                        outputString += if (value == null) {
                            value
                        } else if (value.isConstant()) {
                            // 转换value里的转义字符
                            value.replace("\\n", "\n").replace("\\t", "\t")
                                // 删除首尾的引号（单引号或双引号）
                                .removeSurrounding("\'", "\'").removeSurrounding("\"", "\"")
                        } else {
                            getValueByName(value)
                        }
                    }

                    "read" -> {
                        // 从输入重定向中获取值
                        val value = inputDirection.getOrNull(0) ?: throw Exception("输入不足")
                        // 输入重定向删除第一个元素
                        inputDirection.removeAt(0) ?: throw Exception("输入不足")
                        // 获取结果变量
                        val variable = getVariableByName(qExpression.result)
                        // 赋值
                        variable.value = value
                    }

                    else -> {// 获取函数
                        val function = functionStore.find { it.name == functionName }
                            ?: throw Exception("函数 $functionName 未定义")
                        // 创建一个新状态
                        val newState = Statement(functionName, function.qExpressionList)
                        // 添加到状态表
                        stateStore.add(newState)
                        function.parameterList.forEachIndexed { index, parameter ->
                            val value = parameterStack.getOrNull(index)
                            if (value != null) {
                                newState.addVariable(Variable(parameter.second, value))
                            } else {
                                throw Exception("函数 $functionName 参数不足")
                            }
                        }
                    }
                }
                // 清空参数栈
                parameterStack.clear()
                state.executeIndex++
            }


            // 函数返回
            "return" -> {
                // 获取返回值
                val returnValue = qExpression.value1 // TODO
                // 获取返回值的值
                val realReturnValue = if (returnValue.isConstant()) {
                    returnValue
                } else {
                    getValueByName(returnValue)
                }
                // 设置返回值
                state.returnValue = realReturnValue
                // 删除最新状态
                stateStore.removeLast()
                // 为最新的状态添加返回值
                stateStore.last().getVariableByName("&result")!!.value = realReturnValue
                val funResultName = stateStore.last().qExpressionList.getOrNull(stateStore.last().executeIndex - 1)?.result ?: ""
                val funResultVariable = stateStore.last().getVariableByName(funResultName)
                funResultVariable?.value = realReturnValue
            }

            // continue
            "continue" -> {
                var flag = 1 // 记录循环的层数
                var namespaceFlag = 0 // 记录要删除命名空间的层数
                for (i in state.executeIndex..<qExpressionList.size) { // 从当前指令开始遍历四元式
                    when {
                        qExpressionList[i].instruction == "loopStart" -> { // 遇到循环开始
                            flag++ // 层数加1
                        }

                        qExpressionList[i].instruction == "loopEnd" -> { // 遇到循环结束
                            flag-- // 层数减1
                        }

                        qExpressionList[i].instruction == "end" -> { // 遇到end
                            namespaceFlag++ // 层数加1
                        }

                        qExpressionList[i].instruction == "block" -> { // 遇到block
                            namespaceFlag-- // 层数减1
                        }

                        flag == 1 && qExpressionList[i].instruction == "continueSign" -> { // 找到当前循环的continue
                            state.executeIndex = i + 1 // breakSign的下一条指令
                            if (namespaceFlag > 0) { // 如果要删除的层数大于0
                                // 删除最后namespaceFlag个命名空间
                                for (j in 0..<namespaceFlag) {
                                    // 删除此命名空间里的变量
                                    state.variableStore.removeAll { it.namespace == state.namespaceList.last() }
                                    state.namespaceList.removeLast()
                                }
                            }
                            break
                        }
                    }
                }
            }

            // break
            "break" -> {
                var loopFlag = 1 // 记录循环的层数
                var namespaceFlag = 0 // 记录要删除命名空间的层数
                for (i in state.executeIndex..<qExpressionList.size) { // 从当前指令开始遍历四元式
                    when {
                        qExpressionList[i].instruction == "loopStart" -> { // 遇到循环开始
                            loopFlag++ // 层数加1
                        }

                        qExpressionList[i].instruction == "loopEnd" -> { // 遇到循环结束
                            loopFlag-- // 层数减1
                        }

                        qExpressionList[i].instruction == "end" -> { // 遇到end
                            namespaceFlag++ // 层数加1
                        }

                        qExpressionList[i].instruction == "block" -> { // 遇到block
                            namespaceFlag-- // 层数减1
                        }

                        loopFlag == 1 && qExpressionList[i].instruction == "breakSign" -> { // 找到当前循环的break
                            state.executeIndex = i + 1 // breakSign的下一条指令
                            if (namespaceFlag > 0) { // 如果要删除的层数大于0
                                // 删除最后namespaceFlag个命名空间
                                for (j in 0..<namespaceFlag) {
                                    // 删除此命名空间里的变量
                                    state.variableStore.removeAll { it.namespace == state.namespaceList.last() }
                                    state.namespaceList.removeLast()
                                }
                            }
                            break
                        }
                    }
                }
            }

            // 已正式进入main函数后面申请的变量不再是全局变量
            "fun" -> {
                isAllocState = false
                state.executeIndex++
            }

            // 其余标志节点
            "loopStart", "loopEnd", "continueSign", "breakSign" -> {
                state.executeIndex++
            }

            else -> throw Exception("未知的四元式指令")
        }
    }

    // 执行
    fun executeAll() {
        while (!isEnd) {
            executeStep()
        }
    }
}

// 分割字符串
fun splitString(input: String): List<String> {
    // 如果输入为空字符串或只包含空白，则返回空列表
    if (input.isBlank()) {
        return emptyList()
    }

    // 使用正则表达式 "\\s+" 分隔字符串
    return input.split("\\s+".toRegex())
}

// 判断字符串是否为数字
fun String.isNumber(input: String): Boolean {
    return this.matches(Regex("\\d+"))
}

// 判断字符串是否为常量（字符，字符串，数字）
fun String.isConstant(): Boolean {
    return this.startsWith("\'") || this.startsWith("\"") || isNumber(this)
}


val customTest = """
//函数调用，求一个数的绝对值
int abs(int);

main(){
  int x = -10, y;
  y = abs(x);
  write(y);
}

int abs(int x) {
  if (x <= 0) return -x;
  return x;
}
""".trimIndent()

fun main() {
    // 测试
    val tokenJsStr = codeStringToTokenList(customTest).toJSONString()
    val expressionStore = 语法分析器(tokenJsStr)
    val syntaxTree = expressionStore.toTree().apply { tidy() }
    val umlTree = syntaxTree.toUmlTree()
    println(umlTree)
    outputSVGFile(umlTree, "自定义测试.svg")
    println()
    println("四元式：$customTest")
    // outputSVGFile(umlTree, "自定义测试.svg")
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
    val interpreter = Interpreter(quaternionGeneration, "1")
    val globalVariableStore = interpreter.globalVariableStore ?: listOf()
    var variableStore = interpreter.stateStore.lastOrNull()?.variableStore ?: listOf()
    var tempVariableStore = interpreter.stateStore.lastOrNull()?.tempVariableStore ?: listOf()
    var qExpressions = interpreter.stateStore.lastOrNull()?.qExpressionList ?: listOf()

//    while (interpreter.isEnd) {
//        qExpressions = interpreter.stateStore.lastOrNull()?.qExpressionList ?: listOf()
//        variableStore = interpreter.stateStore.lastOrNull()?.variableStore ?: listOf()
//        tempVariableStore = interpreter.stateStore.lastOrNull()?.tempVariableStore ?: listOf()
//        interpreter.executeStep()
//    }
    interpreter.executeAll()
    println("输出")
    println(interpreter.outputString)
}