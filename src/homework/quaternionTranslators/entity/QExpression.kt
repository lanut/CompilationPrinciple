package homework.quaternionTranslators.entity


data class QExpression(
    var index: Int, // 表达式序号
    val instruction: String, // 四元式指令
    var value1: String, // 3个操作数
    var value2: String, var result: String = "&result"
) {
    override fun toString(): String {
        return "$index ($instruction, $value1, $value2, $result)"
    }
}

fun MutableList<QExpression>.add(
    num: Int, instruction: String, value1: String, value2: String, result: String
) {
    this.add(QExpression(num, instruction, value1, value2, result))
}


