package corewala.buran.io.gemini

import java.lang.StringBuilder

object GemtextHelper {

    /**
     *
     * This is safe for most cases but fails when a line starts with ``` _within_ a code block
     *
     */
    fun findCodeBlocks(source: List<String>): List<String>{
        val sb = StringBuilder()
        var inCodeBlock = false
        val parsed = mutableListOf<String>()
        source.forEach { line ->
            if (line.startsWith("```")) {
                if (!inCodeBlock) {
                    //New code block starting
                    sb.clear()
                    sb.append("```")

                    if(line.length > 3){
                        //Code block has alt text
                        val alt = line.substring(3)
                        sb.append("<|ALT|>$alt</|ALT>")
                    }
                } else {
                    //End of code block
                    parsed.add(sb.toString())
                }
                inCodeBlock = !inCodeBlock
            } else {
                if (inCodeBlock) {
                    sb.append("$line\n")
                } else {
                    parsed.add(line)
                }
            }
        }

        return parsed
    }
}