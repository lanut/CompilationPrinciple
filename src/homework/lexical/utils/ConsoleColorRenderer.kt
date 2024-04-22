package homework.lexical.utils

object ConsoleColorRenderer {

    // ANSI color codes
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BLUE = "\u001B[34m"
    private const val MAGENTA = "\u001B[35m"
    private const val CYAN = "\u001B[36m"
    private const val WHITE = "\u001B[37m"


    fun String.renderRed(): String {
        return "$RED$this$RESET"
    }

    fun String.renderGreen(): String {
        return "$GREEN$this$RESET"
    }

    fun String.renderYellow(): String {
        return "$YELLOW$this$RESET"
    }

    fun String.renderBlue(): String {
        return "$BLUE$this$RESET"
    }

    fun String.renderMagenta(): String {
        return "$MAGENTA$this$RESET"
    }

    fun String.renderCyan(): String {
        return "$CYAN$this$RESET"
    }

    fun String.renderWhite(): String {
        return "$WHITE$this$RESET"
    }

}
