import com.alibaba.fastjson2.toJSONString
import homework.grammatical.entity.SyntaxTree
import homework.grammatical.utils.outputSVGFile
import homework.grammatical.utils.toUmlTree
import homework.grammatical.语法分析器
import homework.lexical.codeStringToTokenList
import homework.lexical.entity.Category
import homework.quaternionTranslators.QuaternionGeneration
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

fun main() {
    val path = Path("./TestFiles/designInput")
    println(path)
    // singleThreadedOutput(path)
    multithreadedOutput(path)
}


fun singleThreadedOutput(path: Path) {
    val directory = File(path.toUri())
    if (!directory.isDirectory) throw Exception("此不是合法的文件夹")
    if (true) { // sample转Token JSON
        val errorList = mutableListOf<String>()
        val sampleFiles = directory.listFiles()
        sampleFiles?.filter {
            it.name.endsWith(".sample")
        }?.filterNotNull()?.forEach { file ->
            val content = File(file.path).readText()
            println("File: ${file.name}")
            val tokens = try {
                codeStringToTokenList(content)
            } catch (e: Exception) {
                val errorName = "词法错误文件名: ${file.path + file.name}"
                println(errorName)
                errorList.add(errorName)
                val sb = StringBuffer()
                sb.appendLine(errorName)
                sb.appendLine(e.stackTraceToString())
                File("./TestFiles/basic/${file.nameWithoutExtension}_LexicalError.txt").writeText(sb.toString())
                return@forEach
            }
            tokens.forEach { token ->
                println(token.toLogStr())
            }
            val jsStr = tokens.filter {
                !(it.category == Category.COMMENT || it.category == Category.ERROR)
            }.toJSONString()
            File("./TestFiles/basic/${file.nameWithoutExtension}.js").writeText(jsStr)
        }
    }

    if (false) {
        val errorList = mutableListOf<String>()
        val jsFiles = directory.listFiles()
        jsFiles?.filter {
            it.name.endsWith(".js")
        }?.filterNotNull()?.forEach { file: File ->
            val content = File(file.path).readText()
            println("File: ${file.name}")
            val expressionStore = try {
                语法分析器(content)
            } catch (e: Exception) {
                val errorName = "语法错误文件名: ${file.path + file.name}"
                println(errorName)
                errorList.add(errorName)
                val sb = StringBuffer()
                sb.appendLine(errorName)
                sb.appendLine(e.stackTraceToString())
                File("./TestFiles/basic/${file.nameWithoutExtension}_GrammaticalError.txt").writeText(sb.toString())
                return@forEach
            }
            println(expressionStore)
            val syntaxTree = expressionStore.toTree()
            val umlTree = syntaxTree.toUmlTree("${file.nameWithoutExtension} 语法树")
            File("./TestFiles/basic/${file.nameWithoutExtension}_Expression.txt").writeText(expressionStore.toString())
            outputSVGFile(umlTree, "./TestFiles/basic/${file.nameWithoutExtension}.svg")
        }
        if (errorList.isNotEmpty()) {
            println("\n语法分析：总计出现${errorList.size}个错误文件")
            errorList.forEach(::println)
        }
    }
}

// 多线程输出：词法分析 -> 语法分析 -> 四元式生成
@OptIn(DelicateCoroutinesApi::class)
fun multithreadedOutput(path: Path) {
    // 创建一个Channel，用于词法分析器和语法分析器之间的通信
    val fileChannel = Channel<File>()
    // 创建一个Channel，用于语法分析器与四元式生成器之间的通信
    val syntaxTreeChannel = Channel<SyntaxTree>()
    val directory = File(path.toUri())
    if (!directory.isDirectory) throw Exception("此不是合法的文件夹")
    val errorList = mutableListOf<String>()


    val 词法分析 = GlobalScope.launch {
        val sampleFiles = directory.listFiles()
        sampleFiles?.filter {
            it.name.endsWith(".sample")
        }?.filterNotNull()?.forEach { file ->

            val content = File(file.path).readText()
            val tokens = try {
                codeStringToTokenList(content)
            } catch (e: Exception) {
                val errorName = "词法错误文件名: ${file.name}"
                errorList.add(errorName)
                val sb = StringBuffer()
                sb.appendLine(errorName)
                sb.appendLine(e.stackTraceToString())
                File("$path/${file.nameWithoutExtension}_Error.txt").writeText(sb.toString())
                return@forEach
            }
            val jsStr = tokens.filter {
                !(it.category == Category.COMMENT || it.category == Category.ERROR)
            }.toJSONString()
            val outputFile = File("$path/${file.nameWithoutExtension}.js")
            outputFile.writeText(jsStr)
            fileChannel.send(outputFile) // 发送文件
        }
        fileChannel.close() // 关闭文件Channel
    }

    val 语法分析 = GlobalScope.launch {
        for (file in fileChannel) {
            val content = File(file.path).readText()
            val expressionStore = try {
                语法分析器(content)
            } catch (e: Exception) {
                val errorName = "语法错误文件名: ${file.name}"
                errorList.add(errorName)
                val sb = StringBuffer()
                sb.appendLine(errorName)
                sb.appendLine(e.stackTraceToString())
                sb.appendLine()
                File("$path/${file.nameWithoutExtension}_Error.txt").writeText(sb.toString())
                continue
            }
            val syntaxTree = expressionStore.toTree(name = file.nameWithoutExtension).apply { tidy() }
            syntaxTreeChannel.send(syntaxTree) // 发送语法树
            val umlTree = syntaxTree.toUmlTree("${file.nameWithoutExtension} 语法树")
            File("$path/${file.nameWithoutExtension}_Expression.txt").writeText(expressionStore.toString())
            outputSVGFile(umlTree, "$path/${file.nameWithoutExtension}.svg")
        }
        syntaxTreeChannel.close() // 关闭语法树Channel
    }

    val 四元式生成 = GlobalScope.launch {
        for (syntaxTree in syntaxTreeChannel) {
            val quaternionGeneration = QuaternionGeneration()
            try {
                quaternionGeneration.程序(syntaxTree.root)
            } catch (e: Exception) {
                val errorName = "四元式生成错误文件名: ${syntaxTree.name}.txt"
                errorList.add(errorName)
                val sb = StringBuffer()
                sb.appendLine(errorName)
                sb.appendLine(e.stackTraceToString())
                sb.appendLine()
                File("$path/${syntaxTree.name}_Error_quaternionGeneration.txt").writeText(sb.toString())
                continue
            }
            val outputFile = File("$path/${syntaxTree.name}_Quaternion.txt")
            val sb = StringBuffer()
            sb.append("main 函数四元式：\n")
            sb.append(quaternionGeneration.qExpressionList.joinToString("\n"))
            for (function in quaternionGeneration.functionList) {
                sb.append("\n\n\n${function.name} 函数参数列表：\n")
                sb.append(function.parameterList.joinToString("\t"))
                sb.append("${function.name} 函数四元式：\n")
                sb.append(function.qExpressionList.joinToString("\n"))
            }
            outputFile.writeText(sb.toString())
        }
    }


    runBlocking {
        词法分析.join()
        println("词法分析完成")
        语法分析.join()
        println("语法分析完成")
        四元式生成.join()
        println("四元式生成完成")
        println()
        if (errorList.isNotEmpty()) {
            val sb = StringBuffer()
            sb.append("总计出现${errorList.size}个错误：\n")
            errorList.forEach {
                if (it.endsWith(".sample")) sb.appendLine("词法错误: $it")
                else if (it.endsWith(".js")) sb.appendLine("语法错误: $it")
            }
            println(sb)
        }
    }
}


