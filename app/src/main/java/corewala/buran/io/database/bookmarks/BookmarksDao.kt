package corewala.buran.io.database.bookmarks

import androidx.room.*

@Dao
interface BookmarksDao {
    @Query("SELECT * FROM bookmarks ORDER BY uiIndex ASC")
    suspend fun getAll(): List<BookmarkEntity>

    @Query("SELECT * from bookmarks WHERE uiIndex = :index LIMIT 1")
    suspend fun getBookmark(index: Int): BookmarkEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookmarks: Array<BookmarkEntity>)

    @Query("UPDATE bookmarks SET uiIndex=:index WHERE uid = :id")
    fun updateUIIndex(id: Int, index: Int)

    @Query("UPDATE bookmarks SET label=:label, uri=:uri WHERE uid = :id")
    fun updateContent(id: Int, label: String, uri: String)

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)
}