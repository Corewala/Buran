package corewala.buran.io.database

import androidx.room.Database
import androidx.room.RoomDatabase
import corewala.buran.io.database.bookmarks.BookmarkEntity
import corewala.buran.io.database.bookmarks.BookmarksDao
import corewala.buran.io.database.history.HistoryDao
import corewala.buran.io.database.history.HistoryEntity

@Database(entities = [BookmarkEntity::class, HistoryEntity::class], version = 3)
abstract class BuranAbstractDatabase: RoomDatabase() {
    abstract fun bookmarks(): BookmarksDao
    abstract fun history(): HistoryDao
}