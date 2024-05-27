package homework.quaternionTranslators

import homework.grammatical.entity.Node
import homework.quaternionTranslators.Type.*
import homework.quaternionTranslators.entity.QExpression
import homework.quaternionTranslators.entity.add


val testStr1 = """
    void fib(int);
    int axd = 9;

    main(){
        int x;
        x = 6 + 5 / 6 - 9;
        fib(5);
    }

    void fib(int x){
        if (x <= 0) {
        write(0);
        } else {
        fib(x-1);
        write(x);
        }
    }
""".trimIndent()


/**
 * 数据类型
 * @property INT 整型
 * @property CHAR 字符型
 * @property FLOAT 浮点型
 * @property VOID 空类型
 */
enum class Type {
    INT, CHAR, FLOAT, VOID
}

///**
// * 变量
// * @property name 变量名
// * @property type 变量类型
// * @property value 变量值
// * @property isConst 是否为常量
// * @property nameSpace 变量所在的命名空间
// */
//data class Variable(
//    val name: String,
//    val type: Type,
//    var value: Int,
//    var isConst: Boolean = false,
//    val nameSpace: String = "main"
//)


/**
 * 函数
 * @property name 函数名
 * @property returnType 返回值类型
 * @property parameterList 参数列表
 * @property qExpressionList 四元式集合
 */
data class Function(
    val name: String = "",
    val returnType: Type = VOID,
    val parameterList: MutableList<Pair<Type, String>> = mutableListOf()
) {
    // 四元式集合
    val qExpressionList = mutableListOf<QExpression>()
}

// 禁用非ASCII字符、函数名、局部变量名警告
@Suppress("NonAsciiCharacters", "FunctionName", "LocalVariableName", "DuplicatedCode")
class QuaternionGeneration {
    val qExpressionList = mutableListOf<QExpression>() // 表达式集合

    // val variableList = mutableListOf<Variable>() // 变量集合
    val functionList = mutableListOf<Function>() // 函数集合
    val infiniteSequence = generateSequence(1) { it + 1 } // 无限序列
    var index = infiniteSequence.iterator() // 序号迭代器

    fun 程序(node: Node) {
        val children = node.children
        for (childNode in children) {
            when (childNode.name) {
                "总全局声明" -> 总全局声明(childNode)
                "Main函数定义" -> Main函数定义(childNode)
                "总函数定义" -> 总函数定义(childNode)
            }
        }
    }

    // <总全局声明> ::=
    fun 总全局声明(node: Node) {
        qExpressionList.add(QExpression(0, "init", "", "", ""))
        val children = node.children
        if (children[0].name == "ε") {
            return
        }
        for (childNode in children) {
            when (childNode.name) {
                "变量声明" -> {
                    val qExpressions = 变量声明(childNode)
                    for (qExpression in qExpressions) {
                        qExpression.index = index.next()
                    }
                    qExpressionList.addAll(qExpressions)
                }

                "函数声明" -> 函数声明(childNode)
            }
        }
    }

    fun 变量声明(node: Node): List<QExpression> {
        val qExpressionList = mutableListOf<QExpression>()
        // 数据类型
        val type = when (node.children[0].children[0].name) {
            "int" -> INT
            "char" -> CHAR
            "float" -> FLOAT
            else -> throw Exception("数据类型错误")
        }
        // 变量节点表
        var variableNodeTableList = mutableListOf<Node>()
        node.children[1].forEach {
            if (it.name == "变量表") {
                variableNodeTableList.add(it)
            }
        }
        variableNodeTableList.forEach { variableTable ->
            val variableException = variableTable.children[0] // 变量式
            val variableName = variableException.children[0].children[0].name // 变量名
            // variableList.add(Variable(variableName, type, 0, false, nameSpace))
            qExpressionList.add(QExpression(0, "alloc", "", "", variableName))
            if (variableException.children.size > 1) { // 说明变量存在初始化
                val expression = 表达式(variableException.children[2].children[0]) // 表达式
                qExpressionList.addAll(expression)
                qExpressionList.add(QExpression(0, "::=", "result", "", variableName))
            }
        }
        return qExpressionList
    }

    fun 函数声明(node: Node) {
        val returnType = when (node.children[0].children[0].name) {
            "int" -> INT
            "char" -> CHAR
            "float" -> FLOAT
            "void" -> VOID
            else -> throw Exception("数据类型错误")
        }
        val functionName = node.children[1].children[0].name // 函数名
        val function = Function(functionName, returnType)
        functionList.add(function)
        val 函数声明形参列表Table = mutableListOf<Node>()
        node.forEach {
            if (it.name == "函数声明形参列表") {
                函数声明形参列表Table.add(it)
            }
        }
        函数声明形参列表Table.forEach { 函数声明形参列表 ->
            val parameterList = mutableListOf<Pair<String, String>>()
            val type = when (node.children[0].children[0].name) {
                "int" -> INT
                "char" -> CHAR
                "float" -> FLOAT
                else -> throw Exception("数据类型错误")
            }
            function.parameterList.add(Pair(type, "temp"))
        }
    }

    fun Main函数定义(node: Node) {
        qExpressionList.add(QExpression(index.next(), "fun", "main", "", ""))
        复合语句(node.children[3])
    }


    fun 复合语句(node: Node) {
        qExpressionList.add(QExpression(index.next(), "block", "", "", ""))
        for (childNode in node.children) {
            when (childNode.name) {
                "局部声明" -> 局部声明(childNode)
                "语句表" -> 语句表(childNode)
            }
        }
        qExpressionList.add(QExpression(index.next(), "end", "", "", ""))
    }

    fun 局部声明(node: Node) {
        val children = node.children
        if (children[0].name == "ε") {
            return
        }
        val allocList = mutableListOf<QExpression>()
        for (childNode in children) {
            when (childNode.name) {
                "变量声明" -> {
                    val qExpressions = 变量声明(childNode)
                    allocList.addAll(qExpressions)
                }
            }
        }
        for (qExpression in allocList) {
            qExpression.index = index.next()
        }
        qExpressionList.addAll(allocList)
    }

    fun 语句表(node: Node) {
        val children = node.children
        if (children[0].name == "ε") {
            return
        }
        for (childNode in children) {
            when (childNode.name) {
                "语句" -> 语句(childNode)
            }
        }
    }

    // <语句> ::= <复合语句> | <if语句> | <while语句> | <for语句> | <doWhile语句> | <return语句> | <break语句> | <continue语句> | <表达式语句>
    fun 语句(node: Node) {
        val children = node.children
        when (children[0].name) {
            "复合语句" -> 复合语句(children[0])
            "if语句" -> if语句(children[0])
            "while语句" -> while语句(children[0])
            "for语句" -> for语句(children[0])
            "doWhile语句" -> doWhile语句(children[0])
            "return语句" -> return语句(children[0])
            "break语句" -> break语句(children[0])
            "continue语句" -> continue语句(children[0])
            "表达式语句" -> 表达式语句(children[0])
        }
    }

    // <if语句> ::= if ( <表达式> ) <语句> <else语句>
    fun if语句(node: Node) {
        val expression = 表达式(index, node.children[2])
        qExpressionList.addAll(expression) // 表达式
        val jmpTure = QExpression(index.next(), "jnz", "&result", "", "") // 跳转真
        val jmpFalse = QExpression(index.next(), "j", "", "", "") // 跳转假
        qExpressionList.add(jmpTure.apply {
            this.result = (jmpFalse.index + 1).toString()
        })
        qExpressionList.add(jmpFalse)
        语句(node.children[4]) // 语句
        // <else语句> ::= else <语句> | ε
        val else语句 = node.children[5]
        if (else语句.children[0].name != "ε") { // else <语句>
            val jmpEnd = QExpression(index.next(), "j", "", "", "") // 跳转结束
            qExpressionList.add(jmpEnd)
            jmpFalse.result = (jmpEnd.index + 1).toString() //回填
            语句(else语句.children[1]) // 语句
            jmpEnd.result = (qExpressionList.last().index + 1).toString()
        } else { // ε
            jmpFalse.result = (qExpressionList.last().index + 1).toString()
        }
    }

    // <while语句> ::= while ( <表达式> ) <语句>
    fun while语句(node: Node) {
        val expression = 表达式(index, node.children[2])
        qExpressionList.addAll(expression) // 表达式
        val jmpTure = QExpression(index.next(), "jnz", "&result", "", "") // 跳转真
        val jmpFalse = QExpression(index.next(), "j", "", "", "") // 跳转假
        qExpressionList.add(jmpTure.apply {
            this.result = (jmpFalse.index + 1).toString()
        })
        qExpressionList.add(jmpFalse)
        语句(node.children[4]) // 语句
        qExpressionList.add(QExpression(index.next(), "continueSign", "", "")) // 添加标记，此语句是跳转到新的循环（处理continue语句）
        val jmpBack = QExpression(index.next(), "j", "", "", expression[0].index.toString())
        qExpressionList.add(jmpBack)
        val breakSign = QExpression(index.next(), "breakSign", "", "")
        qExpressionList.add(breakSign) // 添加标记，此语句将跳出循环
        jmpFalse.result = (breakSign.index + 1).toString()
    }

    // <doWhile语句> ::= do <语句> while ( <表达式> ) ;
    fun doWhile语句(node: Node) {
        val jumpFirst = QExpression(index.next(), "j", "", "", "") // 跳转到while循环语句
        qExpressionList.add(jumpFirst)
        val expression = 表达式(index, node.children[4]) // 表达式
        qExpressionList.addAll(expression)
        val jmpTure = QExpression(index.next(), "jnz", "&result", "", "") // 跳转真
        val jmpFalse = QExpression(index.next(), "j", "", "", "") // 跳转假
        qExpressionList.add(jmpTure.apply {
            this.result = (jmpFalse.index + 1).toString()
        })
        qExpressionList.add(jmpFalse)
        jumpFirst.result = jmpTure.result // 回填跳转到while循环语句
        语句(node.children[1]) // 语句
        qExpressionList.add(QExpression(index.next(), "continueSign", "", "")) // 添加标记，此语句是跳转到新的循环（处理continue语句）
        val jmpBack = QExpression(index.next(), "j", "", "", expression[0].index.toString())
        qExpressionList.add(jmpBack)
        val breakSign = QExpression(index.next(), "breakSign", "", "")
        qExpressionList.add(breakSign) // 添加标记，此语句将跳出循环
        jmpFalse.result = (breakSign.index + 1).toString()
    }

    // <for语句> ::= for ( <for表达式> ; <for表达式> ; <for表达式> ) <语句>
    fun for语句(node: Node) {
        val for表达式 = node.children[2] // for表达式
        val for表达式1 = node.children[4] // for表达式
        val for表达式2 = node.children[6] // for表达式
        val expression1 = 表达式(index, for表达式.children[0]) // 表达式
        val expression2 = 表达式(index, for表达式1.children[0]) // 表达式
        qExpressionList.addAll(expression1)
        qExpressionList.addAll(expression2)
        val jmpTure = QExpression(index.next(), "jnz", "&result", "", "") // 跳转真
        val jmpFalse = QExpression(index.next(), "j", "", "", "") // 跳转假
        qExpressionList.add(jmpTure.apply {
            this.result = (jmpFalse.index + 1).toString()
        })
        qExpressionList.add(jmpFalse)
        语句(node.children[8]) // 语句
        qExpressionList.add(QExpression(index.next(), "continueSign", "", "")) // 添加标记，此语句是跳转到新的循环（处理continue语句）
        val expression3 = 表达式(index, for表达式2.children[0]) // 表达式
        qExpressionList.addAll(expression3)
        val jmpBack = QExpression(index.next(), "j", "", "", expression2[0].index.toString())
        qExpressionList.add(jmpBack)
        val breakSign = QExpression(index.next(), "breakSign", "", "")
        qExpressionList.add(breakSign) // 添加标记，此语句将跳出循环
        jmpFalse.result = (breakSign.index + 1).toString()
    }

    // todo 需要补充一下循环开始标记，用来防止控制语句跳转到错误的位置

    // <break语句> ::= break ;
    fun break语句(node: Node) {
        qExpressionList.add(QExpression(index.next(), "findBreakSign", "", "", "")) // 跳转
    }

    // <continue语句> ::= continue ;
    fun continue语句(node: Node) {
        qExpressionList.add(QExpression(index.next(), "findContinueSign", "", "", "")) // 跳转
    }

    // <return语句> ::= return <表达式语句>
    fun return语句(node: Node) {
        表达式语句(node.children[1]) // 表达式
        qExpressionList.add(QExpression(index.next(), "return", "result", "", "")) // 返回
    }


    // <表达式语句> ::= <表达式> ;
    fun 表达式语句(node: Node) {
        val expression = 表达式(index, node.children[0])
        qExpressionList.addAll(expression)
    }

    // <总函数定义> ::= <函数定义> <总函数定义> | ε
    fun 总函数定义(node: Node) {
        val children = node.children
        if (children[0].name == "ε") {
            return
        }
        for (childNode in children) {
            when (childNode.name) {
                "函数定义" -> 函数定义(childNode)
            }
        }
    }

    // <函数定义> ::= <函数类型> <标识符> ( <函数定义形参列表> ) <复合语句>
    fun 函数定义(node: Node) {
        val returnType = when (node.children[0].children[0].name) {
            "int" -> INT
            "char" -> CHAR
            "float" -> FLOAT
            "void" -> VOID
            else -> throw Exception("数据类型错误")
        }
        val functionName = node.children[1].children[0].name // 函数名
        // 从已经声明的函数表中找到函数
        val function = functionList.find {
            it.name == functionName && it.returnType == returnType
        } ?: throw Exception("函数未声明")
        // 处理函数定义形参列表
        var i = 0// 用于记录参数的位置
        node.forEach { // 修改函数的参数名
            if (it.name == "函数定义形参列表") {
                val funParameter = function.parameterList[i]
                val nodeParameterType = it.children[0].children[0].name // 从语法树中获取参数类型
                // 判断参数类型是否一致
                if (funParameter.first != when (nodeParameterType) {
                        "int" -> INT
                        "char" -> CHAR
                        "float" -> FLOAT
                        "void" -> VOID
                        else -> throw Exception("数据类型错误")
                    }
                ) {
                    throw Exception("参数类型不一致 -> 需要的类型: ${funParameter.first}, 实际的类型: $nodeParameterType, 函数名: $functionName")
                }
                // 修改参数名
                function.parameterList[i] = Pair(funParameter.first, it.children[1].children[0].name)
                i++
            }
        }
        val funQExpressionList = function.qExpressionList // 获取函数的四元式集合
        val quaternionGeneration = QuaternionGeneration() // 新建一个四元式生成器
        val 复合语句 = node.children[5] // 复合语句
        funQExpressionList.add(QExpression(0, "fun", functionName, "", "")) // 函数开始
        quaternionGeneration.复合语句(复合语句) // 生成函数的四元式
        funQExpressionList.addAll(quaternionGeneration.qExpressionList) // 添加函数的四元式
    }


    // <函数调用> ::= <标识符> '(' <实参列表> ')'
    fun 函数调用(node: Node): List<QExpression> {
        val 实参列表 = 实参列表(node.children[2])
        val identifier = node.children[0].children[0].name // 标识符
        return 实参列表 + QExpression(0, "call", identifier, "", "&result")
    }

    // <实参列表> ::= <表达式> | <表达式> ',' <实参列表> | ε
    fun 实参列表(node: Node): List<QExpression> {
        if (node.children[0].name == "ε") {
            return emptyList()
        }
        val expression = 表达式(node.children[0]) + QExpression(0, "param", "&result", "", "")
        if (node.children.size == 1) {
            return expression
        }
        val 实参列表 = 实参列表(node.children[2])
        return expression + 实参列表
    }

    // <表达式> ::= <简单表达式> | <标识符> = <表达式> 返回值为&result
    fun 表达式(index: Iterator<Int>?, node: Node): List<QExpression> {
        // <简单表达式>
        val expression: List<QExpression> = if (node.children.size == 1) {
            简单表达式(node.children[0])
        } else {
            // <标识符> = <表达式>
            val identifier = node.children[0].children[0].name // 标识符
            val expression = 表达式(node.children[2]).toMutableList()
            expression += QExpression(0, "::=", "&result", "", identifier)
            expression
        }

        val resultExceptions = mutableListOf<QExpression>()
        for (i in (0..<expression.size)) { // 优化重复赋值&result的情况
            if (expression[i].instruction == "::=" &&
                expression[i].value1 == "&result" &&
                resultExceptions.last().result == "&result"
            ) {
                resultExceptions.last().result = expression[i].result
                continue
            } else if (expression[i].instruction == "param" &&
                resultExceptions.size > 0 &&
                resultExceptions.last().instruction == "::=" &&
                resultExceptions.last().result == "&result"
            ) { // 函数调用优化：删去赋值&result后直接调用参数的情况
                val value1 = resultExceptions.last().value1
                // 删除 resultExceptions 最后一个元素
                resultExceptions.removeAt(resultExceptions.size - 1)
                resultExceptions.add(expression[i].apply {
                    this.value1 = value1
                })
                continue
            }
            resultExceptions.add(expression[i])
        }

        if (index != null) {
            resultExceptions.forEach {
                it.index = index.next()
            }
        }

        return resultExceptions
    }

    fun 表达式(node: Node): List<QExpression> = 表达式(null, node)

    // <简单表达式> ::= <布尔项> | <布尔项> '||' <简单表达式> 返回值为&result
    fun 简单表达式(node: Node): List<QExpression> {
        // <布尔项>
        if (node.children.size == 1) {
            return 布尔项(node.children[0])
        }
        // <布尔项> '||' <简单表达式>
        // tmpResult1值计算
        val booleanFactor = 布尔项(node.children[0]).toMutableList()
        val tmpResult1 = "&result1_${node.depth}" // 临时结果1
        booleanFactor += QExpression(0, "::=", "&result", "", tmpResult1)

        // 逻辑运算符
        val logicOperator = node.children[1].name

        // tmpResult2值计算
        val tmpResult2 = "&result2_${node.depth}" // 临时结果2
        val simpleExpression = 简单表达式(node.children[2]).toMutableList()
        simpleExpression += QExpression(0, "::=", "&result", "", tmpResult2)
        val result = QExpression(0, logicOperator, tmpResult1, tmpResult2, "&result")
        return booleanFactor + simpleExpression + result
    }

    // <布尔项> ::= <布尔因子> | <布尔因子> '&&' <布尔项> 返回值为&result
    fun 布尔项(node: Node): List<QExpression> {
        // <布尔因子>
        if (node.children.size == 1) {
            return 布尔因子(node.children[0])
        }
        // <布尔因子> '&&' <布尔项>
        // tmpResult1值计算
        val booleanFactor = 布尔因子(node.children[0]).toMutableList()
        val tmpResult1 = "&result1_${node.depth}" // 临时结果1
        booleanFactor += QExpression(0, "::=", "&result", "", tmpResult1)

        // 逻辑运算符
        val logicOperator = node.children[1].name

        // tmpResult2值计算
        val tmpResult2 = "&result2_${node.depth}" // 临时结果2
        val booleanItem = 布尔项(node.children[2]).toMutableList()
        booleanItem += QExpression(0, "::=", "&result", "", tmpResult2)
        val result = QExpression(0, logicOperator, tmpResult1, tmpResult2, "&result")
        return booleanFactor + booleanItem + result
    }

    // <布尔因子> ::= <关系表达式> | <关系表达式> <判断运算符> <布尔因子> 返回值为&result
    fun 布尔因子(node: Node): List<QExpression> {
        // <关系表达式>
        if (node.children.size == 1) {
            return 关系表达式(node.children[0])
        }
        // <关系表达式> <判断运算符> <布尔因子>
        // tmpResult1值计算
        val relationalExpression = 关系表达式(node.children[0]).toMutableList()
        val tmpResult1 = "&result1_${node.depth}" // 临时结果1
        relationalExpression += QExpression(0, "::=", "&result", "", tmpResult1)

        // 判断运算符
        val judgmentOperator = node.children[1].children[0].name

        // tmpResult2值计算
        val tmpResult2 = "&result2_${node.depth}" // 临时结果2
        val booleanFactor = 布尔因子(node.children[2]).toMutableList()
        booleanFactor += QExpression(0, "::=", "&result", "", tmpResult2)
        val result = QExpression(0, judgmentOperator, tmpResult1, tmpResult2, "&result")
        return relationalExpression + booleanFactor + result
    }

    // <关系表达式> ::= <算数表达式> | <算数表达式> <关系运算符> <关系表达式> 返回值为&result
    fun 关系表达式(node: Node): List<QExpression> {
        // <算数表达式>
        if (node.children.size == 1) {
            return 算数表达式(node.children[0])
        }
        // <算数表达式> <关系运算符> <关系表达式>
        // tmpResult1值计算
        val arithmeticExpression = 算数表达式(node.children[0]).toMutableList()
        val tmpResult1 = "&result1_${node.depth}" // 临时结果1
        arithmeticExpression += QExpression(0, "::=", "&result", "", tmpResult1)

        // 关系运算符
        val relationalOperator = node.children[1].children[0].name

        // tmpResult2值计算
        val tmpResult2 = "&result2_${node.depth}" // 临时结果2
        val relationalExpression = 关系表达式(node.children[2]).toMutableList()
        relationalExpression += QExpression(0, "::=", "&result", "", tmpResult2)
        val result = QExpression(0, relationalOperator, tmpResult1, tmpResult2, "&result")
        return arithmeticExpression + relationalExpression + result
    }

    // <算数表达式> ::= <项> | <项> <加减运算符> <算数表达式> 返回值为&result
    fun 算数表达式(node: Node): List<QExpression> {
        // <项>
        if (node.children.size == 1) {
            return 项(node.children[0])
        }
        // <项> <加减运算符> <算数表达式>
        // tmpResult1值计算
        val term = 项(node.children[0]).toMutableList()
        val tmpResult1 = "&result1_${node.depth}" // 临时结果1
        term += QExpression(0, "::=", "&result", "", tmpResult1)

        // 加减运算符
        val addOrSubtractOperator = node.children[1].children[0].name

        // tmpResult2值计算
        val tmpResult2 = "&result2_${node.depth}" // 临时结果2
        val arithmeticExpression = 算数表达式(node.children[2]).toMutableList()
        arithmeticExpression += QExpression(0, "::=", "&result", "", tmpResult2)
        val result = QExpression(0, addOrSubtractOperator, tmpResult1, tmpResult2, "&result")
        return term + arithmeticExpression + result
    }

    // <项> ::= <因子> | <因子> <乘除运算符> <项> 返回值为&result
    fun 项(node: Node): List<QExpression> {
        // <因子>
        if (node.children.size == 1) {
            return 因子(node.children[0])
        }
        // <因子> <乘除运算符> <项>
        // tmpResult1值计算
        val factor = 因子(node.children[0]).toMutableList()
        val tmpResult1 = "&result1_${node.depth}" // 临时结果1
        factor += QExpression(0, "::=", "&result", "", tmpResult1)

        // 乘除运算符
        val multiplyOrDivideOperator = node.children[1].children[0].name

        // tmpResult2值计算
        val tmpResult2 = "&result2_${node.depth}" // 临时结果2
        val term = 项(node.children[2]).toMutableList()
        term += QExpression(0, "::=", "&result", "", tmpResult2)
        val result = QExpression(0, multiplyOrDivideOperator, tmpResult1, tmpResult2, "&result")
        return factor + term + result
    }

    // <因子> ::= <初等量> | <取反运算符> <因子> 返回值为&result
    fun 因子(node: Node): List<QExpression> {
        val tmpExp = mutableListOf<QExpression>()
        if (node.children[0].name == "取反运算符") {
            val negationOperator = node.children[0].children[0].name // 取反运算符
            tmpExp.add(0, negationOperator, "&result", "", "&result")
            return 因子(node.children[1]) + tmpExp
        } else {
            return 初等量(node.children[0])
        }
    }

    // <初等量> ::= <常量> | <变量名> | <函数调用> | <数组调用> | '(' <表达式> ')' 返回值为&result
    fun 初等量(node: Node): List<QExpression> {
        when (node.children[0].name) { // 判断初等量的类型
            "常量" -> { // <常量>
                val value = node.children[0] // "常量"
                    .children[0].name // 常量的值
                return listOf(QExpression(0, "::=", value, "", "&result"))
            }

            "变量名" -> { // <变量名>
                val variableName = node.children[0].children[0].name // 变量名
                return listOf(QExpression(0, "::=", variableName, "", "&result"))
            }

            "函数调用" -> { // <函数调用>
                return 函数调用(node.children[0])
            }

            "数组调用" -> { // <数组调用>
                TODO() // return 数组调用(node.children[0])
            }

            "(" -> { // '(' <表达式> ')'
                return 表达式(node.children[1])
            }
        }
        throw Exception("初等量类型错误")
    }
}

