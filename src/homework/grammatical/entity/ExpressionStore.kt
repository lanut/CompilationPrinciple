package homework.grammatical.entity

import java.util.Stack

/**
 * 存储表达式
 */
class ExpressionStore {
    val nodes = mutableListOf<NodeExpression>()

    // 初始化
    fun init() {
        nodes.clear()
    }

    /**
     * 添加节点
     * @param node 节点
     * @param children 子节点
     */
    fun addNode(node: Node, vararg children: Node) {
        nodes.add(NodeExpression(node, children.toList()))
    }

    override fun toString(): String {
        return nodes.joinToString("\n") {
            "${it.originNode.name} -> ${it.expansionNode.joinToString(" ", transform = Node::name)}"
        }
    }

/*
    fun toTree():SyntaxTree {
        val tree = SyntaxTree(this.nodes[0].originNode.clone())
        var i = 0
        tree.forEach {node ->
            if (i == this.nodes.size) return@forEach
            if (node.name == this.nodes[i].originNode.name) {
                this.nodes[i].expansionNode.forEach{ expansionNode ->
                    node.addChild(expansionNode.clone())
                }
                i++
            }
        }
        return tree
    }
*/
    fun toTree(name: String = "default Name"):SyntaxTree = this.nodes.toTree(name)
}

fun List<NodeExpression>.toTree(name: String):SyntaxTree{
    val tree = SyntaxTree(this[0].originNode.clone(), name)
    var i = 0
    tree.forEach {node ->
        if (i == this.size) return@forEach
        if (node.name == this[i].originNode.name) {
            this[i].expansionNode.forEach{ expansionNode ->
                node.addChild(expansionNode.clone())
            }
            i++
        }
    }
    return tree
}

