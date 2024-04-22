package homework.grammatical.entity

/**
 * 节点表达式，存储计算得出的节点
 * @param originNode 表达式左边的原非终结符
 * @param expansionNode 表达式展开后的集合
 */
data class NodeExpression(
    val originNode: Node,
    // val expansionToken: MutableList<Node> = mutableListOf()
    val expansionNode: List<Node> = emptyList()
)



