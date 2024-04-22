package homework.grammatical.utils

import com.alibaba.fastjson2.into
import homework.grammatical.entity.Category
import homework.grammatical.entity.Token

// 需要fastjson2库(kotlin版)
// Maven坐标为：com.alibaba.fastjson2:fastjson2-kotlin:2.0.48
/**
 * 将json数组转换回Token列表
 */
fun String.jsonArrayToTokenList():List<Token> {
    val mutableList = this.into<List<Token>>().toMutableList()
    mutableList.add(Token(Category.EOF, "EOF", 0))
    return mutableList.asSequence().filter {
        it.category != Category.COMMENT
    }.toList()
}