package corewala.buran.io.database.history

import android.net.Uri

interface HistoryDatasource {

    fun get(onHistory: (List<HistoryEntry>) -> Unit)
    fun add(entry: HistoryEntry, onAdded: () -> Unit)
    fun add(uri: Uri, onAdded: () -> Unit)
    fun clear(onClear: () -> Unit)
    fun delete(entry: HistoryEntry, onDelete: () -> Unit)
}