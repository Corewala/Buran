package corewala.buran.io.history.uris

import android.content.Context

interface HistoryInterface {
    fun add(address: String)
    fun get(): List<String>
    fun clear()

    companion object{
        fun default(context: Context): HistoryInterface {
            return BasicURIHistory(context)
        }
    }
}