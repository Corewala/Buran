package corewala.buran.io.database.history

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import corewala.buran.io.database.BuranAbstractDatabase

class BuranHistory(private val db: BuranAbstractDatabase): HistoryDatasource {

    override fun get(onHistory: (List<HistoryEntry>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val dbBookmarks = db.history().getAll()
            val history = mutableListOf<HistoryEntry>()

            dbBookmarks.forEach { entity ->
                history.add(HistoryEntry(entity.uid, entity.timestamp ?: 0L, Uri.parse(entity.uri)))
            }
            onHistory(history)
        }
    }

    override fun add(entry: HistoryEntry, onAdded: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val lastAdded = db.history().getLastAdded()
            val entity = HistoryEntity(entry.uri.toString(), System.currentTimeMillis())

            when (lastAdded) {
                null -> db.history().insert(entity)
                else -> {
                    when {
                        lastAdded.uri.toString() != entry.uri.toString() -> db.history().insert(entity)
                    }
                }
            }

            onAdded()
        }
    }

    override fun add(uri: Uri, onAdded: () -> Unit) {
        if(!uri.toString().startsWith("gemini://")){
            onAdded
            return
        }
        GlobalScope.launch(Dispatchers.IO){
            val lastAdded = db.history().getLastAdded()
            val entity = HistoryEntity(uri.toString(), System.currentTimeMillis())

            when (lastAdded) {
                null -> db.history().insert(entity)
                else -> {
                    when {
                        lastAdded.uri.toString() != uri.toString() -> db.history().insert(entity)
                    }
                }
            }

            onAdded()
        }
    }

    override fun clear(onClear: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            db.history().clear()
            onClear()
        }
    }

    override fun delete(entry: HistoryEntry, onDelete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val entity = db.history().getEntry(entry.uid)
            db.history().delete(entity)
            onDelete()
        }
    }
}