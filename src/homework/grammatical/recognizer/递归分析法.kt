@file:Suppress("NonAsciiCharacters", "FunctionName", "ClassName")

package homework.grammatical.recognizer

import homework.grammatical.entity.Category
import homework.grammatical.entity.ExpressionStore
import homework.grammatical.entity.Node
import homework.grammatical.utils.BackTokenIterator
import homework.grammatical.utils.match

class 递归分析法 {
    val expressionStore = ExpressionStore()

    // <程序> ::= <总全局声明> <Main函数定义> <总函数定义> <EOF>
    fun 程序(backTokenIterator: BackTokenIterator) {
        expressionStore.init()
        expressionStore.addNode(
            Node("程序"),
            Node("总全局声明"),
            Node("Main函数定义"),
            Node("总函数定义"),
            Node("文件结束")
        )
        总全局声明(backTokenIterator)
        Main函数定义(backTokenIterator)
        总函数定义(backTokenIterator)
        if (!match(backTokenIterator, Category.EOF)) {
            throw Exception("程序识别错误: token: ${backTokenIterator.next()}, 要求的值: 'EOF'")
        }
        expressionStore.addNode(Node("文件结束"), Node("EOF", true))
    }

    // <总全局声明> ::= <全局声明> <总全局声明> | ε
    tailrec fun 总全局声明(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "const") || match(
                backTokenIterator,
                "int"
            ) || match(
                backTokenIterator,
                "float"
            ) || match(backTokenIterator, "char") || match(
                backTokenIterator,
                "void"
            )
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("总全局声明"), Node("全局声明"), Node("总全局声明"))
            全局声明(backTokenIterator)
            总全局声明(backTokenIterator)
        } else {
            expressionStore.addNode(Node("总全局声明"), Node("ε", true))
        }
    }

    // <总函数定义> ::= <函数定义> <总函数定义> | ε
    tailrec fun 总函数定义(backTokenIterator: BackTokenIterator) {
        if (match(
                backTokenIterator,
                "int"
            ) || match(backTokenIterator, "float") || match(
                backTokenIterator,
                "char"
            ) || match(backTokenIterator, "void")
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("总函数定义"), Node("函数定义"), Node("总函数定义"))
            函数定义(backTokenIterator)
            总函数定义(backTokenIterator)
        } else {
            expressionStore.addNode(Node("总函数定义"), Node("ε", true))
        }
    }

    // <Main函数定义> ::= main ( ) <复合语句>
    fun Main函数定义(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("Main函数定义"),
            Node("main", true),
            Node("(", true),
            Node(")", true),
            Node("复合语句")
        )
        if (!match(backTokenIterator, "main")) {
            throw Exception("Main函数定义识别错误: token: ${backTokenIterator.next()}, 要求的值: 'main'")
        }
        if (!match(backTokenIterator, "(")) {
            throw Exception("Main函数定义识别错误: token: ${backTokenIterator.next()}, 要求的值: '('")
        }
        if (!match(backTokenIterator, ")")) {
            throw Exception("Main函数定义识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
        }
        复合语句(backTokenIterator)
    }

    // <函数定义> ::= <函数类型> <标识符> ( <函数定义形参列表> ) <复合语句>
    private fun 函数定义(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("函数定义"),
            Node("函数类型"),
            Node("标识符"),
            Node("(", true),
            Node("函数定义形参列表"),
            Node(")", true),
            Node("复合语句")
        )
        函数类型(backTokenIterator)
        if (!match(backTokenIterator, Category.IDENTIFIER)) {
            throw Exception("函数定义识别错误: token: ${backTokenIterator.next()}, 要求的值: <标识符>")
        }
        backTokenIterator.back()
        val token = backTokenIterator.next()
        expressionStore.addNode(Node("标识符"), Node(token.value, true))
        if (!match(backTokenIterator, "(")) {
            throw Exception("函数定义识别错误: token: ${backTokenIterator.next()}, 要求的值: '('")
        }
        函数定义形参列表(backTokenIterator)
        if (!match(backTokenIterator, ")")) {
            throw Exception("函数定义识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
        }
        复合语句(backTokenIterator)
    }

    // <函数类型> ::= int | float | char | void
    private fun 函数类型(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "int") ||
            match(backTokenIterator, "float") ||
            match(backTokenIterator, "char") ||
            match(backTokenIterator, "void")
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("函数类型"), Node(backTokenIterator.next().value, true))
        } else {
            throw Exception("函数类型识别错误: token: ${backTokenIterator.next()}, 要求的值: 'int', 'float', 'char', 'void'")
        }
    }

    // <数据类型> ::= int | float | char
    fun 数据类型(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "int") ||
            match(backTokenIterator, "float") ||
            match(backTokenIterator, "char")
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("数据类型"), Node(backTokenIterator.next().value, true))
        } else {
            throw Exception("数据类型识别错误: token: ${backTokenIterator.next()}, 要求的值: 'int', 'float', 'char'")
        }
    }

    // <函数定义形参列表> ::= <数据类型> <标识符> <函数定义形参列表尾> | ε
    private fun 函数定义形参列表(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "int") ||
            match(backTokenIterator, "char") ||
            match(backTokenIterator, "void")
        ) {
            backTokenIterator.back()
            expressionStore.addNode(
                Node("函数定义形参列表"),
                Node("数据类型"),
                Node("标识符"),
                Node("函数定义形参列表尾")
            )
            数据类型(backTokenIterator)
            if (!match(backTokenIterator, Category.IDENTIFIER)) {
                throw Exception("函数定义形参列表识别错误: token: ${backTokenIterator.next()}, 要求的值: <标识符>")
            }
            backTokenIterator.back()
            val token = backTokenIterator.next()
            expressionStore.addNode(Node("标识符"), Node(token.value, true))
            函数定义形参列表尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("函数定义形参列表"), Node("ε", true))
        }
    }

    // <函数定义形参列表尾> ::= , <数据类型> <标识符> <函数定义形参列表尾> | ε
    private tailrec fun 函数定义形参列表尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, ",")) {
            expressionStore.addNode(
                Node("函数定义形参列表尾"),
                Node(","),
                Node("数据类型"),
                Node("标识符"),
                Node("函数定义形参列表尾")
            )
            数据类型(backTokenIterator)
            if (!match(backTokenIterator, Category.IDENTIFIER)) {
                throw Exception("函数定义形参列表尾识别错误: token: ${backTokenIterator.next()}, 要求的值: <标识符>")
            }
            backTokenIterator.back()
            val token = backTokenIterator.next()
            expressionStore.addNode(Node("标识符"), Node(token.value, true))
            函数定义形参列表尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("函数定义形参列表尾"), Node("ε", true))
        }
    }

    // <全局声明> ::= <常量声明> | <变量声明> | <函数声明>
    private fun 全局声明(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "const")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("全局声明"), Node("常量声明"))
            常量声明(backTokenIterator)
        } else {
            backTokenIterator.next()
            backTokenIterator.next()
            if (match(backTokenIterator, "(")) {
                backTokenIterator.back()
                backTokenIterator.back()
                backTokenIterator.back()
                expressionStore.addNode(Node("全局声明"), Node("函数声明"))
                函数声明(backTokenIterator)
            } else {
                backTokenIterator.back()
                backTokenIterator.back()
                expressionStore.addNode(Node("全局声明"), Node("变量声明"))
                变量声明(backTokenIterator)
            }
        }
    }

    // <常量声明> ::= const <数据类型> <常量表> ;
    private fun 常量声明(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("常量声明"),
            Node("const", true),
            Node("数据类型"),
            Node("常量表"),
            Node(";", true)
        )
        if (!match(backTokenIterator, "const")) {
            throw Exception("常量声明识别错误: token: ${backTokenIterator.next()}, 要求的值: 'const'")
        }
        数据类型(backTokenIterator)
        常量表(backTokenIterator)
        if (!match(backTokenIterator, ";")) {
            throw Exception("常量声明识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
    }

    // <常量表> ::= <标识符> = <常量> <常量表尾>
    private fun 常量表(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("常量表"), Node("标识符"), Node("=", true), Node("常量"), Node("常量表尾"))
        if (!match(backTokenIterator, Category.IDENTIFIER)) {
            throw Exception("常量表识别错误: token: ${backTokenIterator.next()}, 要求的值: <标识符>")
        }
        backTokenIterator.back()
        val token = backTokenIterator.next()
        expressionStore.addNode(Node("标识符"), Node(token.value, true))
        if (!match(backTokenIterator, "=")) {
            throw Exception("常量表识别错误: token: ${backTokenIterator.next()}, 要求的值: '='")
        }
        if (!match(backTokenIterator, Category.CONSTANT)) {
            throw Exception("常量表识别错误: token: ${backTokenIterator.next()}, 要求的值: <常量>")
        }
        val token2 = backTokenIterator.next()
        expressionStore.addNode(Node("常量"), Node(token2.value, true))
        常量表尾(backTokenIterator)
    }

    // <常量表尾> ::= , <标识符> = <常量> <常量表尾> | ε
    private tailrec fun 常量表尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, ",")) {
            expressionStore.addNode(
                Node("常量表尾"),
                Node(",", true),
                Node("标识符"),
                Node("=", true),
                Node("常量"),
                Node("常量表尾")
            )
            if (!match(backTokenIterator, Category.IDENTIFIER)) {
                throw Exception("常量表尾识别错误: token: ${backTokenIterator.next()}, 要求的值: <标识符>")
            }
            backTokenIterator.back()
            val token = backTokenIterator.next()
            expressionStore.addNode(Node("标识符"), Node(token.value, true))
            if (!match(backTokenIterator, "=")) {
                throw Exception("常量表尾识别错误: token: ${backTokenIterator.next()}, 要求的值: '='")
            }
            if (!match(backTokenIterator, Category.CONSTANT)) {
                throw Exception("常量表尾识别错误: token: ${backTokenIterator.next()}, 要求的值: <常量>")
            }
            val token2 = backTokenIterator.next()
            expressionStore.addNode(Node("常量"), Node(token2.value, true))
            常量表尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("常量表尾"), Node("ε", true))
        }
    }

    // <变量声明> ::= <数据类型> <变量表> ;
    fun 变量声明(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("变量声明"), Node("数据类型"), Node("变量表"), Node(";", true))
        数据类型(backTokenIterator)
        变量表(backTokenIterator)
        if (!match(backTokenIterator, ";")) {
            throw Exception("变量声明识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
    }

    // <变量表> ::= <变量式> <变量表尾>
    fun 变量表(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("变量表"), Node("变量式"), Node("变量表尾"))
        变量式(backTokenIterator)
        变量表尾(backTokenIterator)
    }

    // <变量表尾> ::= , <变量式> <变量表尾> | ε
    tailrec fun 变量表尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, ",")) {
            expressionStore.addNode(Node("变量表尾"), Node(","), Node("变量式"), Node("变量表尾"))
            变量式(backTokenIterator)
            变量表尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("变量表尾"), Node("ε", true))
        }
    }

    // <变量式> ::= <标识符> <变量式尾>
    fun 变量式(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("变量式"), Node("标识符"), Node("变量式尾"))
        if (!match(backTokenIterator, Category.IDENTIFIER)) {
            throw Exception("变量式识别错误: token: ${backTokenIterator.next()}, 要求的值: <标识符>")
        }
        backTokenIterator.back()
        val token = backTokenIterator.next()
        expressionStore.addNode(Node("标识符"), Node(token.value, true))
        变量式尾(backTokenIterator)
    }

    // <变量式尾> ::= = <表达式> | ε
    fun 变量式尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "=")) {
            expressionStore.addNode(Node("变量式尾"), Node("=", true), Node("表达式"))
            表达式(backTokenIterator)
        } else {
            expressionStore.addNode(Node("变量式尾"), Node("ε", true))
        }
    }

    // <函数声明> ::= <函数类型> <标识符> ( <函数声明形参列表> ) ;
    private fun 函数声明(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("函数声明"),
            Node("函数类型"),
            Node("标识符"),
            Node("(", true),
            Node("函数声明形参列表"),
            Node(")", true),
            Node(";", true)
        )
        函数类型(backTokenIterator)
        if (!match(backTokenIterator, Category.IDENTIFIER)) {
            throw Exception("函数声明识别错误: token: ${backTokenIterator.next()}, 要求的值: <标识符>")
        }
        backTokenIterator.back()
        val token = backTokenIterator.next()
        expressionStore.addNode(Node("标识符"), Node(token.value, true))
        if (!match(backTokenIterator, "(")) {
            throw Exception("函数声明识别错误: token: ${backTokenIterator.next()}, 要求的值: '('")
        }
        函数声明形参列表(backTokenIterator)
        if (!match(backTokenIterator, ")")) {
            throw Exception("函数声明识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
        }
        if (!match(backTokenIterator, ";")) {
            throw Exception("函数声明识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
    }

    // <函数声明形参列表> ::= <数据类型> <函数声明形参列表尾> | ε
    private fun 函数声明形参列表(backTokenIterator: BackTokenIterator) {
        if (match(
                backTokenIterator,
                "int"
            ) || match(backTokenIterator, "char") || match(
                backTokenIterator,
                "void"
            )
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("函数声明形参列表"), Node("数据类型"), Node("函数声明形参列表尾"))
            数据类型(backTokenIterator)
            函数声明形参列表尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("函数声明形参列表"), Node("ε", true))
        }
    }

    // <函数声明形参列表尾> ::= , <数据类型> <函数声明形参列表尾> | ε
    private tailrec fun 函数声明形参列表尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, ",")) {
            expressionStore.addNode(Node("函数声明形参列表尾"), Node(","), Node("数据类型"), Node("函数声明形参列表尾"))
            数据类型(backTokenIterator)
            函数声明形参列表尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("函数声明形参列表尾"), Node("ε", true))
        }
    }

    // <复合语句> ::= { <局部声明> <语句表> }
    fun 复合语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("复合语句"),
            Node("{", true),
            Node("局部声明"),
            Node("语句表"),
            Node("}", true)
        )
        if (!match(backTokenIterator, "{")) {
            throw Exception("复合语句识别错误: token: ${backTokenIterator.next()}, 要求的值: '{'")
        }
        局部声明(backTokenIterator)
        语句表(backTokenIterator)
        if (!match(backTokenIterator, "}")) {
            throw Exception("复合语句识别错误: token: ${backTokenIterator.next()}, 要求的值: '}'")
        }
    }

    // <局部声明> ::= <变量声明> <局部声明> | ε
    fun 局部声明(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "int") ||
            match(backTokenIterator, "char") ||
            match(backTokenIterator, "void")
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("局部声明"), Node("变量声明"), Node("局部声明"))
            变量声明(backTokenIterator)
            局部声明(backTokenIterator)
        } else {
            expressionStore.addNode(Node("局部声明"), Node("ε", true))
        }
    }

    // <语句表> ::= <语句> <语句表> | ε
    fun 语句表(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "{") ||
            match(backTokenIterator, "if") ||
            match(backTokenIterator, "while") ||
            match(backTokenIterator, "for") ||
            match(backTokenIterator, "do") ||
            match(backTokenIterator, "return") ||
            match(backTokenIterator, "break") ||
            match(backTokenIterator, "continue") ||
            match(backTokenIterator, Category.IDENTIFIER) ||
            match(backTokenIterator, "(") ||
            match(backTokenIterator, Category.CONSTANT)
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句表"), Node("语句"), Node("语句表"))
            语句(backTokenIterator)
            语句表(backTokenIterator)
        } else {
            expressionStore.addNode(Node("语句表"), Node("ε", true))
        }
    }

    // <语句> ::= <复合语句> | <if语句> | <while语句> | <for语句> | <doWhile语句> | <return语句> | <break语句> | <continue语句> | <表达式语句>
    fun 语句(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "{")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句"), Node("复合语句"))
            复合语句(backTokenIterator)
        } else if (match(backTokenIterator, "if")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句"), Node("if语句"))
            if语句(backTokenIterator)
        } else if (match(backTokenIterator, "while")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句"), Node("while语句"))
            while语句(backTokenIterator)
        } else if (match(backTokenIterator, "for")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句"), Node("for语句"))
            for语句(backTokenIterator)
        } else if (match(backTokenIterator, "do")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句"), Node("doWhile语句"))
            doWhile语句(backTokenIterator)
        } else if (match(backTokenIterator, "return")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句"), Node("return语句"))
            return语句(backTokenIterator)
        } else if (match(backTokenIterator, "break")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句"), Node("break语句"))
            break语句(backTokenIterator)
        } else if (match(backTokenIterator, "continue")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("语句"), Node("continue语句"))
            continue语句(backTokenIterator)
        } else {
            expressionStore.addNode(Node("语句"), Node("表达式语句"))
            表达式语句(backTokenIterator)
        }
    }

    // <if语句> ::= if ( <表达式> ) <语句> <else语句>
    private fun if语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("if语句"),
            Node("if", true),
            Node("(", true),
            Node("表达式"),
            Node(")", true),
            Node("语句"),
            Node("else语句")
        )
        if (!match(backTokenIterator, "if")) {
            throw Exception("if语句识别错误: token: ${backTokenIterator.next()}, 要求的值: 'if'")
        }
        if (!match(backTokenIterator, "(")) {
            throw Exception("if语句识别错误: token: ${backTokenIterator.next()}, 要求的值: '('")
        }
        表达式(backTokenIterator)
        if (!match(backTokenIterator, ")")) {
            throw Exception("if语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
        }
        语句(backTokenIterator)
        else语句(backTokenIterator)
    }

    // <else语句> ::= else <语句> | ε
    private fun else语句(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "else")) {
            expressionStore.addNode(Node("else语句"), Node("else", true), Node("语句"))
            语句(backTokenIterator)
        } else {
            expressionStore.addNode(Node("else语句"), Node("ε", true))
        }
    }

    // <for语句> ::= for ( <for表达式> ; <for表达式> ; <for表达式> ) <语句>
    private fun for语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("for语句"),
            Node("for", true),
            Node("(", true),
            Node("for表达式"),
            Node(";", true),
            Node("for表达式"),
            Node(";", true),
            Node("for表达式"),
            Node(")", true),
            Node("语句")
        )
        if (!match(backTokenIterator, "for")) {
            throw Exception("for语句识别错误: token: ${backTokenIterator.next()}, 要求的值: 'for'")
        }
        if (!match(backTokenIterator, "(")) {
            throw Exception("for语句识别错误: token: ${backTokenIterator.next()}, 要求的值: '('")
        }
        for表达式(backTokenIterator)
        if (!match(backTokenIterator, ";")) {
            throw Exception("for语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
        for表达式(backTokenIterator)
        if (!match(backTokenIterator, ";")) {
            throw Exception("for语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
        for表达式(backTokenIterator)
        if (!match(backTokenIterator, ")")) {
            throw Exception("for语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
        }
        语句(backTokenIterator)
    }

    // <for表达式> ::= <表达式> | ε
    private fun for表达式(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, Category.IDENTIFIER) || match(backTokenIterator, "(") || match(
                backTokenIterator,
                Category.CONSTANT
            )
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("for表达式"), Node("表达式"))
            表达式(backTokenIterator)
        } else {
            expressionStore.addNode(Node("for表达式"), Node("ε", true))
        }
    }

    // <while语句> ::= while ( <表达式> ) <语句>
    private fun while语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("while语句"),
            Node("while", true),
            Node("(", true),
            Node("表达式"),
            Node(")", true),
            Node("语句")
        )
        if (!match(backTokenIterator, "while")) {
            throw Exception("while语句识别错误: token: ${backTokenIterator.next()}, 要求的值: 'while'")
        }
        if (!match(backTokenIterator, "(")) {
            throw Exception("while语句识别错误: token: ${backTokenIterator.next()}, 要求的值: '('")
        }
        表达式(backTokenIterator)
        if (!match(backTokenIterator, ")")) {
            throw Exception("while语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
        }
        语句(backTokenIterator)
    }

    // <doWhile语句> ::= do <语句> while ( <表达式> ) ;
    private fun doWhile语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(
            Node("doWhile语句"),
            Node("do", true),
            Node("语句"),
            Node("while", true),
            Node("(", true),
            Node("表达式"),
            Node(")", true),
            Node(";", true)
        )
        if (!match(backTokenIterator, "do")) {
            throw Exception("doWhile语句识别错误: token: ${backTokenIterator.next()}, 要求的值: 'do'")
        }
        语句(backTokenIterator)
        if (!match(backTokenIterator, "while")) {
            throw Exception("doWhile语句识别错误: token: ${backTokenIterator.next()}, 要求的值: 'while'")
        }
        if (!match(backTokenIterator, "(")) {
            throw Exception("doWhile语句识别错误: token: ${backTokenIterator.next()}, 要求的值: '('")
        }
        表达式(backTokenIterator)
        if (!match(backTokenIterator, ")")) {
            throw Exception("doWhile语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
        }
        if (!match(backTokenIterator, ";")) {
            throw Exception("doWhile语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
    }

    // <return语句> ::= return <表达式语句>
    private fun return语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("return语句"), Node("return", true), Node("表达式语句"))
        if (!match(backTokenIterator, "return")) {
            throw Exception("return语句识别错误: token: ${backTokenIterator.next()}, 要求的值: 'return'")
        }
        表达式语句(backTokenIterator)
    }

    // <break语句> ::= break ;
    private fun break语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("break语句"), Node("break", true), Node(";", true))
        if (!match(backTokenIterator, "break")) {
            throw Exception("break语句识别错误: token: ${backTokenIterator.next()}, 要求的值: 'break'")
        }
        if (!match(backTokenIterator, ";")) {
            throw Exception("break语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
    }

    // <continue语句> ::= continue ;
    private fun continue语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("continue语句"), Node("continue", true), Node(";", true))
        if (!match(backTokenIterator, "continue")) {
            throw Exception("continue语句识别错误: token: ${backTokenIterator.next()}, 要求的值: 'continue'")
        }
        if (!match(backTokenIterator, ";")) {
            throw Exception("continue语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
    }

    // <表达式语句> ::= <表达式> ;
    fun 表达式语句(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("表达式语句"), Node("表达式"), Node(";", true))
        表达式(backTokenIterator)
        if (!match(backTokenIterator, ";")) {
            throw Exception("表达式语句识别错误: token: ${backTokenIterator.next()}, 要求的值: ';'")
        }
    }

    // <表达式> ::= <简单表达式> | <标识符> = <表达式> // 此处两处first集合有冲突，需要修改为识别第二个Token
    tailrec fun 表达式(backTokenIterator: BackTokenIterator) {
        val token1 = backTokenIterator.next()
        val token2 = backTokenIterator.next()
        backTokenIterator.back()
        backTokenIterator.back()
        if (token1.category == Category.IDENTIFIER && token2.value == "=") {
            expressionStore.addNode(Node("表达式"), Node("标识符"), Node("=", true), Node("表达式"))
            val token = backTokenIterator.next()
            expressionStore.addNode(Node("标识符"), Node(token.value, true))
            if (!match(backTokenIterator, "=")) {
                throw Exception("表达式识别错误: token: ${backTokenIterator.next()}, 要求的值: '='")
            }
            表达式(backTokenIterator)
        } else {
            expressionStore.addNode(Node("表达式"), Node("简单表达式"))
            简单表达式(backTokenIterator)
        }
    }


    // <简单表达式> ::= <布尔项> <简单表达式尾>
    fun 简单表达式(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("简单表达式"), Node("布尔项"), Node("简单表达式尾"))
        布尔项(backTokenIterator)
        简单表达式尾(backTokenIterator)
    }

    // <简单表达式尾> ::= '||' <布尔项> <简单表达式尾> | ε
    tailrec fun 简单表达式尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "||")) {
            expressionStore.addNode(Node("简单表达式尾"), Node("||", true), Node("布尔项"), Node("简单表达式尾"))
            布尔项(backTokenIterator)
            简单表达式尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("简单表达式尾"), Node("ε", true))
        }
    }

    // <布尔项> ::= <布尔因子> <布尔项尾>
    fun 布尔项(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("布尔项"), Node("布尔因子"), Node("布尔项尾"))
        布尔因子(backTokenIterator)
        布尔项尾(backTokenIterator)
    }

    // <布尔项尾> ::= '&&' <布尔因子> <布尔项尾> | ε
    tailrec fun 布尔项尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "&&")) {
            expressionStore.addNode(Node("布尔项尾"), Node("&&", true), Node("布尔因子"), Node("布尔项尾"))
            布尔因子(backTokenIterator)
            布尔项尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("布尔项尾"), Node("ε", true))
        }
    }

    // <布尔因子> ::= <关系表达式> <布尔因子尾>
    fun 布尔因子(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("布尔因子"), Node("关系表达式"), Node("布尔因子尾"))
        关系表达式(backTokenIterator)
        布尔因子尾(backTokenIterator)
    }

    // <布尔因子尾> ::= <判断运算符> <关系表达式> <布尔因子尾> | ε
    tailrec fun 布尔因子尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "==") || match(
                backTokenIterator,
                "!="
            )
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("布尔因子尾"), Node("判断运算符"), Node("关系表达式"), Node("布尔因子尾"))
            判断运算符(backTokenIterator)
            关系表达式(backTokenIterator)
            布尔因子尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("布尔因子尾"), Node("ε", true))
        }
    }

    // <判断运算符> ::= '==' | '!='
    private fun 判断运算符(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "==")) {
            expressionStore.addNode(Node("判断运算符"), Node("==", true))
        } else if (match(backTokenIterator, "!=")) {
            expressionStore.addNode(Node("判断运算符"), Node("!=", true))
        } else {
            throw Exception("判断运算符识别错误: token: ${backTokenIterator.next()}, 要求的值: '==' | '!='")
        }
    }

    // <关系表达式> ::= <算数表达式> <关系表达式尾>
    fun 关系表达式(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("关系表达式"), Node("算数表达式"), Node("关系表达式尾"))
        算数表达式(backTokenIterator)
        关系表达式尾(backTokenIterator)
    }

    // <关系表达式尾> ::= <关系运算符> <算数表达式> <关系表达式尾> | ε
    tailrec fun 关系表达式尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "<") || match(
                backTokenIterator,
                ">"
            ) || match(backTokenIterator, "<=") || match(
                backTokenIterator, ">="
            )
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("关系表达式尾"), Node("关系运算符"), Node("算数表达式"), Node("关系表达式尾"))
            关系运算符(backTokenIterator)
            算数表达式(backTokenIterator)
            关系表达式尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("关系表达式尾"), Node("ε", true))
        }
    }

    // <关系运算符> ::= '<' | '>' | '<=' | '>='
    private fun 关系运算符(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "<")) {
            expressionStore.addNode(Node("关系运算符"), Node("<", true))
        } else if (match(backTokenIterator, ">")) {
            expressionStore.addNode(Node("关系运算符"), Node(">", true))
        } else if (match(backTokenIterator, "<=")) {
            expressionStore.addNode(Node("关系运算符"), Node("<=", true))
        } else if (match(backTokenIterator, ">=")) {
            expressionStore.addNode(Node("关系运算符"), Node(">=", true))
        } else {
            throw Exception("关系运算符识别错误: token: ${backTokenIterator.next()}, 要求的值: '<' | '>' | '<=' | '>='")
        }
    }

    // <算数表达式> ::= <项> <算数表达式尾>
    fun 算数表达式(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("算数表达式"), Node("项"), Node("算数表达式尾"))
        项(backTokenIterator)
        算数表达式尾(backTokenIterator)
    }

    // <算数表达式尾> ::= <加减运算符> <项> <算数表达式尾> | ε
    tailrec fun 算数表达式尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "+") || match(
                backTokenIterator,
                "-"
            )
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("算数表达式尾"), Node("加减运算符"), Node("项"), Node("算数表达式尾"))
            加减运算符(backTokenIterator)
            项(backTokenIterator)
            算数表达式尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("算数表达式尾"), Node("ε", true))
        }
    }

    // <加减运算符> ::= '+' | '-'
    fun 加减运算符(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "+")) {
            expressionStore.addNode(Node("加减运算符"), Node("+", true))
        } else if (match(backTokenIterator, "-")) {
            expressionStore.addNode(Node("加减运算符"), Node("-", true))
        } else {
            throw Exception("加减运算符识别错误: token: ${backTokenIterator.next()}, 要求的值: '+' | '-'")
        }
    }

    // <项> ::= <因子> <项尾>
    fun 项(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("项"), Node("因子"), Node("项尾"))
        因子(backTokenIterator)
        项尾(backTokenIterator)
    }

    // <项尾> ::= <乘除运算符> <因子> <项尾> | ε
    tailrec fun 项尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "*") || match(
                backTokenIterator,
                "/"
            ) || match(backTokenIterator, "%")
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("项尾"), Node("乘除运算符"), Node("因子"), Node("项尾"))
            乘除运算符(backTokenIterator)
            因子(backTokenIterator)
            项尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("项尾"), Node("ε", true))
        }
    }

    // <乘除运算符> ::= '*' | '/' | '%'
    fun 乘除运算符(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "*")) {
            expressionStore.addNode(Node("乘除运算符"), Node("*", true))
        } else if (match(backTokenIterator, "/")) {
            expressionStore.addNode(Node("乘除运算符"), Node("/", true))
        } else if (match(backTokenIterator, "%")) {
            expressionStore.addNode(Node("乘除运算符"), Node("%", true))
        } else {
            throw Exception("乘除运算符识别错误: token: ${backTokenIterator.next()}, 要求的值: '*' | '/' | '%'")
        }
    }

    // <因子> ::= <初等量> | <取反运算符> <因子>
    fun 因子(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, Category.CONSTANT) ||
            match(backTokenIterator, Category.IDENTIFIER) ||
            match(backTokenIterator, Category.CHAR) ||
            match(backTokenIterator, Category.STRING) ||
            match(backTokenIterator, "(")
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("因子"), Node("初等量"))
            初等量(backTokenIterator)
        } else if (match(backTokenIterator, "-") || match(
                backTokenIterator,
                "!"
            )
        ) {
            backTokenIterator.back()
            expressionStore.addNode(Node("因子"), Node("取反运算符"), Node("因子"))
            取反运算符(backTokenIterator)
            因子(backTokenIterator)
        } else {
            throw Exception("因子识别错误: token: ${backTokenIterator.next()}, 要求的值: <初等量> | <取反运算符>")
        }
    }

    // <取反运算符> ::= '-' | '!'
    private fun 取反运算符(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "-")) {
            expressionStore.addNode(Node("取反运算符"), Node("-", true))
        } else if (match(backTokenIterator, "!")) {
            expressionStore.addNode(Node("取反运算符"), Node("!", true))
        } else {
            throw Exception("取反运算符识别错误: token: ${backTokenIterator.next()}, 要求的值: '-' | '!'")
        }
    }

    // <初等量> ::= <常量> |
//            ( <表达式> ) |
//            <标识符> <引用>
    fun 初等量(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, Category.CONSTANT) ||
            match(backTokenIterator, Category.CHAR) ||
            match(backTokenIterator, Category.STRING)
        ) {
            expressionStore.addNode(Node("初等量"), Node("常量"))
            backTokenIterator.back()
            expressionStore.addNode(Node("常量"), Node(backTokenIterator.next().value, true))
        } else if (match(backTokenIterator, "(")) {
            expressionStore.addNode(Node("初等量"), Node("(", true), Node("表达式"), Node(")", true))
            表达式(backTokenIterator)
            if (!match(backTokenIterator, ")")) {
                throw Exception("初等量识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
            }
        } else if (match(backTokenIterator, Category.IDENTIFIER)) {
            expressionStore.addNode(Node("初等量"), Node("标识符"), Node("引用"))
            backTokenIterator.back()
            val token = backTokenIterator.next()
            expressionStore.addNode(Node("标识符"), Node(token.value, true))
            引用(backTokenIterator)
        } else {
            throw Exception("初等量识别错误: token: ${backTokenIterator.next()}, 要求的值: <常量> | '('  | <标识符> ")
        }
    }

    // <引用> ::= '(' <实参列表> ')' | '[' <整数列表> ']' | ε
    fun 引用(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, "(")) {
            expressionStore.addNode(Node("引用"), Node("(", true), Node("实参列表"), Node(")", true))
            实参列表(backTokenIterator)
            if (!match(backTokenIterator, ")")) {
                throw Exception("引用识别错误: token: ${backTokenIterator.next()}, 要求的值: ')'")
            }
        } else if (match(backTokenIterator, "[")) {
            expressionStore.addNode(Node("引用"), Node("[", true), Node("整数列表"), Node("]", true))
            整数列表(backTokenIterator)
            if (!match(backTokenIterator, "]")) {
                throw Exception("引用识别错误: token: ${backTokenIterator.next()}, 要求的值: ']'")
            }
        } else {
            expressionStore.addNode(Node("引用"), Node("ε", true))
        }
    }

    // <实参列表> ::= <表达式> <实参列表尾>
    fun 实参列表(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, ")")) {
            backTokenIterator.back()
            expressionStore.addNode(Node("实参列表"), Node("ε", true))
            return
        }
        expressionStore.addNode(Node("实参列表"), Node("表达式"), Node("实参列表尾"))
        表达式(backTokenIterator)
        实参列表尾(backTokenIterator)
    }

    // <实参列表尾> ::= ',' <表达式>  <实参列表尾> | ε
    tailrec fun 实参列表尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, ",")) {
            expressionStore.addNode(Node("实参列表尾"), Node(","), Node("表达式"), Node("实参列表尾"))
            表达式(backTokenIterator)
            实参列表尾(backTokenIterator)
        } else {
            expressionStore.addNode(Node("实参列表尾"), Node("ε", true))
        }
    }

    // <整数列表> ::= <整数> <整数列表尾>
    private fun 整数列表(backTokenIterator: BackTokenIterator) {
        expressionStore.addNode(Node("整数列表"), Node("整数"), Node("整数列表尾"))
        val token = backTokenIterator.next()
        if (token.category == Category.CONSTANT && token.value.toIntOrNull() != null) {
            expressionStore.addNode(Node("整数"), Node(token.value, true))
            整数列表尾(backTokenIterator)
        } else {
            throw Exception("整数列表识别错误: token: ${backTokenIterator.next()}, 要求的值: <整数>")
        }
    }

    // <整数列表尾> ::= ',' <整数> <整数列表尾> | ε
    private tailrec fun 整数列表尾(backTokenIterator: BackTokenIterator) {
        if (match(backTokenIterator, ",")) {
            expressionStore.addNode(Node("整数列表尾"), Node(",", true), Node("整数"), Node("整数列表尾"))
            val token = backTokenIterator.next()
            if (token.category == Category.CONSTANT && token.value.toIntOrNull() != null) {
                expressionStore.addNode(Node("整数"), Node(token.value, true))
                整数列表尾(backTokenIterator)
            } else {
                throw Exception("整数列表尾识别错误: token: ${backTokenIterator.next()}, 要求的值: <整数>")
            }
        } else {
            expressionStore.addNode(Node("整数列表尾"), Node("ε", true))
        }
    }

}