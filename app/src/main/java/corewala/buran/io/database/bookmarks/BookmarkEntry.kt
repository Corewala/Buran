package corewala.buran.io.database.bookmarks

import java.net.URI

class BookmarkEntry(
    val uid: Int,
    val label: String,
    val uri: URI,
    val index: Int
){
    var visible = true
}