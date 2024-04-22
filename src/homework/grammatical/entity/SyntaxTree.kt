package homework.grammatical.entity


/**
 * 语法分析树的节点
 * @param name 节点名称
 * @param isTerminalSymbols 是否为终结符
 * @param children 子节点列表
 * @param depth 节点深度
 * @param parent 父节点
 * @param index 节点索引
 */
data class Node(
    val name: String,
    var isTerminalSymbols: Boolean = false,
    val children: MutableList<Node> = mutableListOf(),
    var depth: Int = 0,
    var parent: Node? = null,
    var index: Int = 0,
) {
    /**
     * 添加子节点
     * @param node 子节点
     */
    fun addChild(node: Node) {
        node.depth = this.depth + 1
        node.parent = this
        children.add(node)
    }

    /**
     * 遍历节点
     * @param action 对每个节点的操作
     */
    fun forEach(action: (Node) -> Unit) {
        action(this)
        children.forEach { it.forEach(action) }
    }

    /**
     * 查找节点
     * @param name 节点名称
     * @return 节点
     */
    fun findNode(name: String): Node? {
        return if (this.name == name) {
            this
        } else {
            children.asSequence().map { it.findNode(name) }.firstOrNull { it != null }
        }
    }

    /**
     * 获取叶子节点
     * @return 叶子节点列表
     */
    fun getLeafNodes(): List<Node> {
        return if (children.isEmpty()) {
            listOf(this)
        } else {
            children.flatMap { it.getLeafNodes() }
        }
    }

    /**
     * 获取节点数量
     * @return 节点数量
     */
    fun getNodeCount(): Int {
        return 1 + children.sumOf { it.getNodeCount() }
    }

    /**
     * 获取子树深度
     * @return 子树深度
     */
    fun getSubTreeDepth(): Int {
        return if (children.isEmpty()) {
            1
        } else {
            children.maxOf { it.getSubTreeDepth() } + 1
        }
    }

    /**
     * 获取下一个索引的兄弟节点，如果没有，则返回父节点的下一个兄弟节点，如果没有继续递归。
     * <b>可能只需要深度遍历就行了，不需要此函数</b>
     * @return 下一个兄弟节点
     */
    fun getNextSibling(): Node {
        return if (parent == null) {
            throw Exception("此节点没有下一个兄弟节点")
        } else {
            val index = parent!!.children.indexOf(this)
            if (index + 1 < parent!!.children.size) {
                parent!!.children[index + 1]
            } else {
                parent!!.getNextSibling()
            }
        }
    }

    /**
     * 重写toString方法
     */
    override fun toString(): String {
        return "Node(name='$name', children=${
            children.joinToString(prefix = "[", postfix = "]") {
                it.name
            }
        }, depth=$depth, parent=${parent?.name}, index=$index)"
    }


    fun clone():Node {
        val node = Node(this.name)
        node.isTerminalSymbols = this.isTerminalSymbols
        node.depth = this.depth
        node.parent = this.parent
        node.index = this.index
        this.children.forEach {
            node.addChild(it.clone())
        }
        return node
    }
}

/**
 * 语法分析树
 * @param root 根节点
 */
data class SyntaxTree(
    val root: Node
) {
    /**
     * 添加节点到根节点
     * @param node 根节点
     */
    fun addToRoot(node: Node) {
        root.addChild(node)
    }

    /**
     * 遍历节点
     * @param action 对每个节点的操作
     */
    fun forEach(action: (Node) -> Unit) {
        root.forEach(action)
    }

    /**
     * 查找节点
     * @param name 节点名称
     * @return 节点
     */
    fun findNode(name: String): Node? {
        return root.findNode(name)
    }


    /**
     * 获取叶子节点
     * @return 叶子节点列表
     */
    fun getLeafNodes(): List<Node> {
        return root.getLeafNodes()
    }

    /**
     * 获取节点数量
     * @return 节点数量
     */
    fun getNodeCount(): Int {
        return root.getNodeCount()
    }

    /**
     * 获取子树深度
     * @return 子树深度
     */
    fun getSubTreeDepth(): Int {
        return root.getSubTreeDepth()
    }
}

