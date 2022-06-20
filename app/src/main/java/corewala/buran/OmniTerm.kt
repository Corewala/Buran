package corewala.buran

import android.net.Uri
import java.util.*

const val GEM_SCHEME = "gemini://"

class OmniTerm(private val listener: Listener) {
    val history = ArrayList<OppenURI>()
    var uri = OppenURI()
    var penultimate = OppenURI()

    /**
     * User input to the 'omni bar' - could be an address or a search term
     * @param term - User-inputted term
     */
    fun input(term: String, searchbase: String?){
        when {
            term.contains(" ") -> {
                val encoded = Uri.encode(term)
                listener.request("$searchbase$encoded")
            }
            term.startsWith(GEM_SCHEME) && term != GEM_SCHEME -> {
                listener.request(term)
                return
            }
            term.contains(".") -> {
                listener.request("gemini://${term}")
            }
            else -> {
                val encoded = Uri.encode(term)
                listener.request("$searchbase$encoded")
            }
        }
    }

    fun search(term: String, searchbase: String?){
        val encoded = Uri.encode(term)
        listener.request("$searchbase$encoded")
    }


    fun navigation(link: String) {
        navigation(link, true)
    }

    fun imageAddress(link: String){
        navigation(link, false)
    }

    /**
     * A clicked link, could be absolute or relative
     * @param link - a Gemtext link
     */
    private fun navigation(link: String, invokeListener: Boolean) {
        when {
            link.startsWith(GEM_SCHEME) -> uri.set(link)
            link.startsWith("//") -> uri.set("gemini:$link")
            link.contains(":") -> listener.openExternal(link)
            else -> uri.resolve(link)
        }

        val address = uri.toString().replace("//", "/").replace("gemini:/", "gemini://")
        println("OmniTerm resolved address: $address")

        if(invokeListener) listener.request(address)
    }

    fun reset(){
        uri = penultimate.copy()
    }

    fun set(address: String) {
        penultimate.set(address)
        uri.set(address)
        if (history.isEmpty() || history.last().toString() != address) {
            history.add(uri.copy())
        }
    }

    fun getCurrent(): String {
        return if(history.size > 0){
            history.last().toString()
        }else{
            ""
        }
    }

    fun canGoBack(): Boolean {
        return history.size > 1
    }

    fun goBack(): String {
        history.removeLast()
        return history.last().toString()
    }

    fun clearCache() {
        history.clear()
    }

    interface Listener{
        fun request(address: String)
        fun openExternal(address: String)
    }
}