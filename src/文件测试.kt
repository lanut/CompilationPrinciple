import com.alibaba.fastjson2.toJSONString
import homework.grammatical.utils.outputSVGFile
import homework.grammatical.utils.toUmlTree
import homework.grammatical.语法分析器
import homework.lexical.codeStringToTokenList
import homework.lexical.entity.Category
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

fun main() {
    val path = Path("./TestFiles/basic")
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


@OptIn(DelicateCoroutinesApi::class)
fun multithreadedOutput(path: Path) {
    // 创建一个Channel，用于在生产者和消费者之间传递File对象
    val fileChannel = Channel<File>()
    val directory = File(path.toUri())
    if (!directory.isDirectory) throw Exception("此不是合法的文件夹")
    val errorList = mutableListOf<String>()

    // 生产者协程
    val producer = GlobalScope.launch {

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
                File("./TestFiles/basic/${file.nameWithoutExtension}_Error.txt").writeText(sb.toString())
                return@forEach
            }
            val jsStr = tokens.filter {
                !(it.category == Category.COMMENT || it.category == Category.ERROR)
            }.toJSONString()
            val outputFile = File("./TestFiles/basic/${file.nameWithoutExtension}.js")
            outputFile.writeText(jsStr)
            fileChannel.send(outputFile)
        }
        fileChannel.close()
    }

    // 消费者协程
    val consumer = GlobalScope.launch {
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
                File("./TestFiles/basic/${file.nameWithoutExtension}_Error.txt").writeText(sb.toString())
                continue
            }
            val syntaxTree = expressionStore.toTree()
            val umlTree = syntaxTree.toUmlTree("${file.nameWithoutExtension} 语法树")
            File("./TestFiles/basic/${file.nameWithoutExtension}_Expression.txt").writeText(expressionStore.toString())
            outputSVGFile(umlTree, "./TestFiles/basic/${file.nameWithoutExtension}.svg")
        }
    }


    // 等待生产者和消费者协程完成
    runBlocking {
        producer.join()
        consumer.join()
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


