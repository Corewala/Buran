package corewala.buran.io.history.uris

import android.content.Context

/**
 *
 * Another shared prefs implementation so I don't get slowed down by a Room implementation at this point
 *
 */
class BasicURIHistory(context: Context): HistoryInterface {

    private val DELIM = "||"
    private val prefsKey = "history.BasicURIHistory.prefsKey"
    private val prefsHistoryKey = "history.BasicURIHistory.prefsHistoryKey"
    private val prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)

    override fun add(address: String) {

        val history = get()

        when {
            history.size >= 50 -> history.removeAt(0)
        }

        if(history.isNotEmpty() && history.size > 10){
            if(history.subList(history.size - 10, history.size).contains(address)) return
        }

        history.add(address)
        val raw = history.joinToString(DELIM)
        prefs.edit().putString(prefsHistoryKey, raw).apply()
    }

    override fun clear(){
        prefs.edit().clear().apply()
    }

    override fun get(): ArrayList<String> {
        return when (val raw = prefs.getString(prefsHistoryKey, null)) {
            null -> arrayListOf()
            else -> ArrayList(raw.split(DELIM))
        }
    }
}