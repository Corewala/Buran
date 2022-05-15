package corewala.buran

const val SCHEME = "gemini://"
const val TRAVERSE = "../"
const val SOLIDUS = "/"
const val DIREND = "/"

/**
 *
 * Easy uri path handling for Gemini
 *
 */
class OppenURI constructor(private var ouri: String) {

    constructor(): this("")

    var host: String = ""

    init {
        extractHost()
    }

    fun set(ouri: String){
        this.ouri = ouri
        extractHost()
    }

    fun resolve(reference: String): String{
        if(ouri == "$SCHEME$host") ouri = "$ouri/"
        when {
            reference.startsWith(SCHEME) -> set(reference)
            reference.startsWith(SOLIDUS) -> ouri = "$SCHEME$host$reference"
            reference.startsWith(TRAVERSE) -> {
                if(!ouri.endsWith(DIREND)) ouri = ouri.removeFile()
                val traversalCount = reference.split(TRAVERSE).size - 1
                ouri = traverse(traversalCount) + reference.replace(TRAVERSE, "")
            }
            else -> {
                ouri = when {
                    ouri.endsWith(DIREND) -> "${ouri}$reference"
                    else -> "${ouri.substring(0, ouri.lastIndexOf("/"))}/$reference"
                }
            }
        }
        return ouri
    }

    fun traverse(): OppenURI{
        val path = ouri.removePrefix("$SCHEME$host")
        val segments  = path.split(SOLIDUS).filter { it.isNotEmpty() }

        var nouri = "$SCHEME$host"

        when (ouri) {
            "" -> {
            }
            SCHEME -> ouri = ""
            "$nouri/" -> ouri = SCHEME
            else -> {
                when {
                    segments.isNotEmpty() -> {
                        val remaining = segments.dropLast(1)
                        remaining.forEach { segment ->
                            nouri += "/$segment"
                        }
                        ouri = "$nouri/"
                    }
                    else -> ouri = "$nouri/"
                }
            }
        }

        return this
    }

    private fun traverse(count: Int): String{
        val path = ouri.removePrefix("$SCHEME$host")
        val segments  = path.split(SOLIDUS).filter { it.isNotEmpty() }
        val segmentCount = segments.size
        var nouri = "$SCHEME$host"

        segments.forEachIndexed{ index, segment ->
            if(index < segmentCount - count){
                nouri += "/$segment"
            }
        }

        return "$nouri/"

    }

    private fun extractHost(){
        if(ouri.isEmpty()) return
        val urn = ouri.removePrefix(SCHEME)
        host = when {
            urn.contains(SOLIDUS) -> urn.substring(0, urn.indexOf(SOLIDUS))
            else -> urn
        }
    }

    fun copy(): OppenURI = OppenURI(ouri)

    override fun toString(): String = ouri

    private fun String.removeFile(): String{
        return this.substring(0, ouri.lastIndexOf("/") + 1)
    }
}
