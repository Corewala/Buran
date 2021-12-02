package corewala.buran.io.gemini

object GeminiResponse {

    const val INPUT = 1
    const val SUCCESS = 2
    const val REDIRECT = 3
    const val TEMPORARY_FAILURE = 4
    const val PERMANENT_FAILURE = 5
    const val CLIENT_CERTIFICATE_REQUIRED = 6
    const val UNKNOWN = -1

    fun parseHeader(header: String): Header {
        val cleanHeader = header.replace("\\s+".toRegex(), " ")
        val meta: String
        when {
            header.startsWith("2") -> {
                val segments = cleanHeader.trim().split(" ")
                meta = when {
                    segments.size > 1 -> segments[1]
                    else -> "text/gemini; charset=utf-8"
                }
            }
            else -> {

                meta = when {
                    cleanHeader.contains(" ") -> cleanHeader.substring(cleanHeader.indexOf(" ") + 1)
                    else -> cleanHeader
                }
            }
        }

        return when {
            header.startsWith("1") -> Header(
                INPUT,
                meta
            )
            header.startsWith("2") -> Header(
                SUCCESS,
                meta
            )
            header.startsWith("3") -> Header(
                REDIRECT,
                meta
            )
            header.startsWith("4") -> Header(
                TEMPORARY_FAILURE,
                meta
            )
            header.startsWith("5") -> Header(
                PERMANENT_FAILURE,
                meta
            )
            header.startsWith("6") -> Header(
                CLIENT_CERTIFICATE_REQUIRED,
                meta
            )
            else -> Header(
                UNKNOWN,
                meta
            )
        }
    }

    fun getCodeString(code: Int): String{
        return when(code){
            1 -> "Input"
            2 -> "Success"
            3 -> "Redirect"
            4 -> "Temporary Failure"
            5 -> "Permanent Failure"
            6 -> "Client Certificate Required"
            -3 -> "Client Certificate Error"
            -2 -> "Bad response: Server Error"
            -1 -> "Connection Error"
            else -> "Unknown: $code"
        }
    }

    class Header(val code: Int, val meta: String)
}