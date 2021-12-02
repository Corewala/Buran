package corewala.buran.io.database.bookmarks

interface BookmarksDatasource {

    fun get(onBookmarks: (List<BookmarkEntry>) -> Unit)
    fun add(bookmarkEntry: BookmarkEntry, onAdded: () -> Unit)
    fun add(bookmarkEntries: Array<BookmarkEntry>, onAdded: () -> Unit)
    fun delete(bookmarkEntry: BookmarkEntry, onDelete: () -> Unit)

    fun moveUp(bookmarkEntry: BookmarkEntry, onMoved: () -> Unit)
    fun moveDown(bookmarkEntry: BookmarkEntry, onMoved: () -> Unit)
    fun update(bookmarkEntry: BookmarkEntry, label: String?, uri: String?, onUpdate: () -> Unit)
}