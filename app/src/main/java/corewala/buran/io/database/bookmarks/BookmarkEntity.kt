package corewala.buran.io.database.bookmarks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
class BookmarkEntity(
    @ColumnInfo(name = "label") val label: String?,
    @ColumnInfo(name = "uri") val uri: String?,
    @ColumnInfo(name = "uiIndex") val uiIndex: Int?,
    @ColumnInfo(name = "folder") val folder: String?
){
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}