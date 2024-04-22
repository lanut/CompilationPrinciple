package homework.lexical.entity

/**
 * Token 的存储类
 */
class TokenStore {
    val tokens = mutableListOf<Token>() // Token 的列表
    // 初始化
    fun init() {
        tokens.clear()
    }
}