package corewala.buran.io.database

import android.content.Context
import androidx.room.Room
import corewala.buran.io.database.bookmarks.BuranBookmarks
import corewala.buran.io.database.history.BuranHistory

class BuranDatabase(context: Context) {

    private val db: BuranAbstractDatabase = Room.databaseBuilder(context, BuranAbstractDatabase::class.java, "buran_database_v1").build()

    fun bookmarks(): BuranBookmarks = BuranBookmarks(db)
    fun history(): BuranHistory = BuranHistory(db)
}