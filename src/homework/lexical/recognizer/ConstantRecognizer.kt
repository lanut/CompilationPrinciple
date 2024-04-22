package homework.lexical.recognizer

import homework.lexical.entity.Category
import homework.lexical.entity.SampleWords
import homework.lexical.entity.Token
import homework.lexical.entity.TokenStore.tokens
import homework.lexical.recognizer.ConstantRecognizer.State.*
import homework.lexical.utils.BackCharIterator
import homework.lexical.utils.isBlankOrNewLine


class ConstantRecognizer {
    // state 表示当前的状态
    var state = S0

    // buffer 用于存储识别的字符
    val buffer = StringBuilder()

    enum class State {
        S0, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, ERROR, End;
    }
}


/**
 * 识别常量
 * @param charIterator 字符迭代器
 * @param line 行号
 */
fun constantRecognizer(charIterator: BackCharIterator, line: Int) {
    val constantRecognizer = ConstantRecognizer()
    while (charIterator.hasNext() && constantRecognizer.state != ERROR && constantRecognizer.state != End) {
        val char = charIterator.nextChar()
        // 此处的DFA识别在课本P47
        when (constantRecognizer.state) {
            S0 -> {
                if (char == '0') {
                    constantRecognizer.state = S3
                    constantRecognizer.buffer.append(char)
                } else {
                    constantRecognizer.state = S1
                    constantRecognizer.buffer.append(char)
                }
            }

            S1 -> {
                when {
                    char.isDigit() -> {
                        constantRecognizer.buffer.append(char)
                    }

                    (char == 'e' || char == 'E') -> {
                        constantRecognizer.state = S10
                        constantRecognizer.buffer.append(char)
                    }

                    char == '.' -> {
                        constantRecognizer.state = S8
                        constantRecognizer.buffer.append(char)
                    }

                    char.isBlankOrOperatorOrDelimiter() -> {
                        constantRecognizer.state = End
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S2 -> {
                when {
                    char.isBlankOrOperatorOrDelimiter() -> {
                        constantRecognizer.state = End
                    }

                    char in '0'..'7' -> {
                        constantRecognizer.buffer.append(char)
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S3 -> {
                when {
                    char.isBlankOrOperatorOrDelimiter() || (char.toString() in SampleWords.operators) || (char.toString() in SampleWords.delimiters) -> {
                        constantRecognizer.state = End
                    }

                    char == '.' -> {
                        constantRecognizer.state = S8
                        constantRecognizer.buffer.append(char)
                    }

                    char in '0'..'7' -> {
                        constantRecognizer.state = S2
                        constantRecognizer.buffer.append(char)
                    }

                    char == 'x' || char == 'X' -> {
                        constantRecognizer.state = S5
                        constantRecognizer.buffer.append('x')
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S4 -> Unit
            S5 -> {
                when {
                    char.isDigit() || char in 'a'..'f' || char in 'A'..'F' -> {
                        constantRecognizer.state = S6
                        constantRecognizer.buffer.append(
                            if (char in 'a'..'f') {
                                char.uppercase()
                            } else {
                                char
                            }
                        )
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S6 -> {
                when {
                    char.isDigit() || char in 'a'..'f' || char in 'A'..'F' -> {
                        constantRecognizer.buffer.append(
                            if (char in 'a'..'f') {
                                char.uppercase()
                            } else {
                                char
                            }
                        )
                    }

                    char.isBlankOrOperatorOrDelimiter() -> {
                        constantRecognizer.state = End
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S7 -> Unit
            S8 -> { // 现在是小数点之后的状态
                when {
                    char.isDigit() -> {
                        constantRecognizer.state = S9
                        constantRecognizer.buffer.append(char)
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S9 -> {
                when {
                    char.isDigit() -> {
                        constantRecognizer.buffer.append(char)
                    }

                    char.isBlankOrOperatorOrDelimiter() -> {
                        constantRecognizer.state = End
                    }

                    (char == 'e' || char == 'E') -> {
                        constantRecognizer.buffer.append('e')
                        constantRecognizer.state = S10
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S10 -> {
                when {
                    char == '+' || char == '-' -> {
                        constantRecognizer.state = S11
                        constantRecognizer.buffer.append(char)
                    }

                    char.isDigit() -> {
                        constantRecognizer.state = S12
                        constantRecognizer.buffer.append(char)
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S11 -> {
                when {
                    char.isDigit() -> {
                        constantRecognizer.state = S12
                        constantRecognizer.buffer.append(char)
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S12 -> {
                when {
                    char.isDigit() -> {
                        constantRecognizer.buffer.append(char)
                    }

                    char.isBlankOrOperatorOrDelimiter() -> {
                        constantRecognizer.state = End
                    }

                    else -> {
                        constantRecognizer.state = ERROR
                    }
                }
            }

            S13 -> Unit
            S14 -> Unit
            S15 -> Unit
            ERROR -> Unit
            End -> Unit
        }
    }

    // 如果当前状态为 End，则将字符迭代器回退一个字符
    if (constantRecognizer.state == End) {
        charIterator.back()
    }

    // 如果状态为ERROR 则遍历到空白或者遍历完，添加到ERROR Token
    if (constantRecognizer.state == ERROR) {
        charIterator.back()
        while (charIterator.hasNext()) {
            val char = charIterator.nextChar()
            if (char.isBlankOrNewLine()) {
                break
            }
            constantRecognizer.buffer.append(char)
        }
        tokens.add(Token(Category.ERROR, constantRecognizer.buffer.toString(), line))
        return
    }
    tokens.add(Token(Category.CONSTANT, constantRecognizer.buffer.toString(), line))
}

// 判断是否数字后面是否合法
fun Char.isBlankOrOperatorOrDelimiter(): Boolean {
    return this.isBlankOrNewLine() || this.toString() in SampleWords.delimiters || this.toString() in SampleWords.operators
}