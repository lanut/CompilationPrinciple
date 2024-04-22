package homework.grammatical.utils

import homework.grammatical.entity.SyntaxTree
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 需要PlantUML库
 * 链接如下: [github](https://github.com/plantuml/plantuml) 下载jar文件
 * Maven坐标: **net.sourceforge.plantuml:plantuml:1.2024.4**
 */


/**
 * 导出SVG格式的PlantUML图（文本）
 * @param umlStr PlantUML格式的字符串
 * @return svg格式的字符串文本
 */
fun outputSVGString(umlStr: String): String {
    val reader = SourceStringReader(umlStr)
    // 输出为SVG格式
    val stream = ByteArrayOutputStream()
    // 输出到OutputStream
    reader.outputImage(stream, FileFormatOption(FileFormat.SVG))
    stream.close()
    // 获取生成的SVG代码
    return String(stream.toByteArray(), Charset.forName("UTF-8"))
}


/**
 * 导出SVG格式的PlantUML图到指定路径
 * @param umlStr PlantUML格式的字符串
 * @param pathname 输出的文件路径（默认为./test.svg）
 * @return svg格式的字符串文本
 */
fun outputSVGFile(umlStr: String, pathname: String = "./test.svg") {
    val reader = SourceStringReader(umlStr)
    val stream = ByteArrayOutputStream()
    reader.outputImage(stream, FileFormatOption(FileFormat.SVG))
    Files.write(Paths.get(pathname), stream.toByteArray())
    stream.close()
}

fun SyntaxTree.toUmlTree(title: String = "Sample语法树"): String {
    val stringBuffer = StringBuffer()
    stringBuffer.append(
        """
        @startmindmap
        title: $title
        <style>
        mindmapDiagram {
          .表达式 {BackgroundColor #F08080}
          .复合语句 {BackgroundColor #FFA07A}
          .函数形参列表 {BackgroundColor #90EE90}
          .定义 {BackgroundColor #87CEFA}
          .语句 {BackgroundColor #FFDEAD}
          .声明 {BackgroundColor #FFB6C1}
          .全局声明 {BackgroundColor #778899}
          .局部声明 {BackgroundColor #B0C4DE}
          .初等量 {BackgroundColor #20B2AA}
          .Main定义 {BackgroundColor #EE82EE}
        }
        </style>
        
    """.trimIndent()
    )
    this.forEach {
        var tmpStr = ""
        for (i in 0..it.depth) {
            tmpStr += "*"
        }
        if (it.isTerminalSymbols) {
            tmpStr += "_"
        }
        val style = when {
            it.name == "Main函数定义" -> "<<Main定义>>"
            it.name == ("复合语句") -> "<<复合语句>>"
            it.name == ("声明") -> "<<声明>>"
            it.name == ("全局声明") -> "<<全局声明>>"
            it.name == ("局部声明") -> "<<局部声明>>"
            it.name == ("初等量") -> "<<初等量>>"
            it.name.contains("语句") -> "<<语句>>"
            it.name.contains("定义") -> "<<定义>>"
            it.name.contains("表达式") -> "<<表达式>>"
            it.name.contains("布尔") -> "<<函数形参列表>>"
            else -> ""
        }

        val name = if (it.name == "=") "\"=\"" else it.name
        stringBuffer.appendLine("$tmpStr $name $style")
    }
    stringBuffer.append(
        """
        @endmindmap
    """.trimIndent()
    )
    return stringBuffer.toString()
}