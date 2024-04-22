import com.alibaba.fastjson2.toJSONString
import homework.grammatical.utils.outputSVGFile
import homework.grammatical.utils.toUmlTree
import homework.grammatical.语法分析器
import homework.lexical.codeStringToTokenList
import homework.lexical.entity.Category
import java.io.File


fun main() {
    val directory = File("./TestFiles/basic")

    if (true) { // sample转Token JSON
        val sampleFiles = directory.listFiles()
        sampleFiles?.filter {
            it.name.endsWith(".sample")
        }?.filterNotNull()?.forEach { file ->
            // val content = Files.readAllLines(file.toPath()).joinToString("\n")
            val content = File(file.path).readText()
            println("File: ${file.name}")
            val tokens = try {
                codeStringToTokenList(content)
            } catch (e: Exception) {
                TODO("Not yet implemented")
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

    if (true) {
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
                val errorName = "错误文件名: ${file.path + file.name}"
                println(errorName)
                errorList.add(errorName)
                val sb = StringBuffer()
                sb.appendLine(errorName)
                // sb.appendLine(e.toString())
                sb.appendLine(e.stackTraceToString())
                File("./TestFiles/basic/${file.nameWithoutExtension}_Error.txt").writeText(sb.toString())
                return@forEach
            }
            println(expressionStore)
            val syntaxTree = expressionStore.toTree()
            val umlTree = syntaxTree.toUmlTree("${file.nameWithoutExtension} 语法树")
            File("./TestFiles/basic/${file.nameWithoutExtension}_Expression.txt").writeText(expressionStore.toString())
            outputSVGFile(umlTree, "./TestFiles/basic/${file.nameWithoutExtension}.svg")
        }
    }
}