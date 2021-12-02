package corewala.buran.io.database.history

import android.net.Uri

class HistoryEntry(
    val uid: Int,
    val timestamp: Long,
    val uri: Uri
)