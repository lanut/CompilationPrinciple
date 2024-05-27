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
    var name: String,
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
        addChild(children.size, node)
    }

    /**
     * 添加子节点到指定位置
     * @param index 位置
     * @param node 子节点
     */
    fun addChild(index: Int, node: Node) {
        node.depth = this.depth + 1
        node.parent = this
        children.add(index, node)
        // 整理children子树的深度
        node.forEach {
            // 如果没有父节点，则不需要修改深度
            it.depth = (it.parent?.depth ?: 0) + 1
        }
    }

    /**
     * 删除指定位置子节点
     * @param index 位置
     * @return 删除的子节点
     */
    fun removeChild(index: Int): Node {
        return children.removeAt(index).apply {
            this.parent = null
            this.depth = 0
        }
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
     * <u>可能只需要深度遍历就行了，不需要此函数</u>
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

    /**
     * 克隆节点
     * @return 克隆的节点
     */
    fun clone(): Node {
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
    val root: Node,
    val name: String
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

    /**
     * 整理语法树,(将双目运算符整理到父节点上)
     */
    fun tidy() {
        // 边遍历边修改会造成不安全，因此需要先收集节点，在修改节点
        val nodesToModify = mutableListOf<Pair<Node, Node>>() // 需要修改尾递归的节点
        val nodesToDelete = mutableListOf<Pair<Node, Node>>() // 需要删除的节点
        val nodesToAdd = mutableListOf<Pair<Node, Node>>() // 需要添加的节点

        val nodeNamesRegex = ".*尾".toRegex() // 匹配尾部的节点

        if (root.name == "程序") {
            var 总全局声明 = root.children[0]
            if (总全局声明.children.size > 1) {
                nodesToDelete.add(Pair(总全局声明, 总全局声明.children[0]))
                nodesToDelete.add(Pair(总全局声明, 总全局声明.children[1]))
                nodesToAdd.add(Pair(总全局声明, 总全局声明.children[0].children[0]))
            }

            root.forEach {
                if (it.name == "总全局声明" && it.depth > 1) {
                    if (it.children.size == 1 && it.children[0].name == "ε") {
                        nodesToDelete.add(Pair(it.parent!!, it))
                        return@forEach
                    }
                    val child = it.children[0].children[0]
                    nodesToAdd.add(Pair(总全局声明, child))
                }
            }
        }
        // 收集需要修改的节点
        root.forEach {
            when {
                it.name.matches(nodeNamesRegex) -> {
                    if (it.name == "变量式尾") {it.name = "变量赋值后缀尾"}
                    val parent = it.parent!! // 此节点的父节点
                    val children = it.removeChild(0) // 存放运算符的节点
                    // 修改尾递归节点
                    if (children.name != "ε") { // 修改节点
                        nodesToModify.add(Pair(parent, children))
                    } else { // 删除空节点
                        it.parent?.let { parent ->
                            nodesToDelete.add(Pair(parent, it))
                        }
                    }
                    it.name = it.name.dropLast(1) // 删除尾部的尾，如：项尾 -> 项
                }

                it.name == "初等量" && it.children.size == 2 -> { // 用于记录数组或者函数的节点 <标识符> <引用>
                    val functionNode = it.children[0] // <标识符>
                    val referenceNode = it.children[1] // <引用>
                    when (referenceNode.children[0].name) {
                        "ε" -> { // 当it节点只有变量名时
                            nodesToDelete.add(Pair(it, referenceNode))
                            it.children[0].name = "变量名"
                        }

                        "(" -> {// 当it节点为函数调用时
                            nodesToDelete.add(Pair(it, functionNode))
                            nodesToDelete.add(Pair(it, referenceNode))
                            val funNode = Node("函数调用")
                            nodesToAdd.add(Pair(it, funNode))
                            nodesToAdd.add(Pair(funNode, functionNode))
                            nodesToAdd.add(Pair(funNode, referenceNode.children[0]))
                            nodesToAdd.add(Pair(funNode, referenceNode.children[1]))
                            nodesToAdd.add(Pair(funNode, referenceNode.children[2]))
                        }

                        "[" -> {// 当it节点为数组调用时
                            nodesToDelete.add(Pair(it, functionNode))
                            nodesToDelete.add(Pair(it, referenceNode))
                            val arrayNode = Node("数组调用")
                            nodesToAdd.add(Pair(it, arrayNode))
                            nodesToAdd.add(Pair(arrayNode, functionNode))
                            nodesToAdd.add(Pair(arrayNode, referenceNode.children[0]))
                            nodesToAdd.add(Pair(arrayNode, referenceNode.children[1]))
                            nodesToAdd.add(Pair(arrayNode, referenceNode.children[2]))
                        }
                    }
                }

                it.name == "局部声明" && it.parent?.name == "复合语句" -> {
                    if (it.children.size == 1 && it.children[0].name == "ε") {
                        return@forEach
                    }
                    nodesToDelete.add(Pair(it, it.children[1]))
                    it.children[1].forEach { childNode ->
                        if (childNode.name == "局部声明" && childNode.children.size == 2) {
                            nodesToAdd.add(Pair(it, childNode.children[0]))
                        }
                    }
                }

                it.name == "复合语句" -> {
                    val 语句表 = it.children[2]
                    // 如果复合语句下的语句表只有一个空节点，跳过
                    if (语句表.children.size == 1 && 语句表.children[0].name == "ε") {
                        return@forEach
                    }
                    var 子语句表 = 语句表.children.last()
                    nodesToDelete.add(Pair(语句表, 子语句表))
                    while (子语句表.children.size == 2) {
                        val 语句 = 子语句表.children[0]
                        nodesToAdd.add(Pair(语句表, 语句))
                        子语句表 = 子语句表.children[1]
                    }
                }

                it.name == "总函数定义" -> {
                    if (it.children.size == 1 && it.children[0].name == "ε") {
                        return@forEach
                    }
                    var 子总函数定义 = it.children[1]
                    nodesToDelete.add(Pair(it, 子总函数定义))
                    while (子总函数定义.children.size == 2) {
                        val 函数定义 = 子总函数定义.children[0]
                        nodesToAdd.add(Pair(it, 函数定义))
                        子总函数定义 = 子总函数定义.children[1]
                    }
                }

            }
        }

        // 修改尾递归节点
        nodesToModify.forEach { (parent, children) ->
            val size = parent.children.size // 父节点的子节点数
            try {
                parent.addChild(size - 1, children)
            } catch (e: Exception) {
                println(parent)
                println(children)
                e.printStackTrace()
            }
        }

        // 删除节点
        nodesToDelete.forEach { (parent, children) ->
            parent.children.remove(children)
        }

        // 添加节点
        nodesToAdd.forEach { (parent, children) ->
            parent.addChild(children)
        }

    }
}


