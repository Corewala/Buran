package corewala.buran.io.database.bookmarks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import corewala.buran.io.database.BuranAbstractDatabase
import java.net.URI

class BuranBookmarks(private val db: BuranAbstractDatabase): BookmarksDatasource {

    override fun get(onBookmarks: (List<BookmarkEntry>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val dbBookmarks = db.bookmarks().getAll()
            val bookmarks = mutableListOf<BookmarkEntry>()

            dbBookmarks.forEach { bookmarkEntity ->
                bookmarks.add(
                    BookmarkEntry(
                        uid = bookmarkEntity.uid,
                        label = bookmarkEntity.label ?: "Unknown",
                        uri = URI.create(bookmarkEntity.uri),
                        index = bookmarkEntity.uiIndex ?: 0)
                )
            }
            onBookmarks(bookmarks)
        }
    }

    override fun add(bookmarkEntry: BookmarkEntry, onAdded: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val bookmarkEntity = BookmarkEntity(
                label = bookmarkEntry.label,
                uri = bookmarkEntry.uri.toString(),
                uiIndex = bookmarkEntry.index,
                folder = "~/")

            db.bookmarks().insertAll(arrayOf(bookmarkEntity))
            onAdded()
        }
    }

    override fun add(bookmarkEntries: Array<BookmarkEntry>, onAdded: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val entities = bookmarkEntries.map { entry ->
                BookmarkEntity(
                    label = entry.label,
                    uri = entry.uri.toString(),
                    uiIndex = entry.index,
                    folder = "~/")
            }
            db.bookmarks().insertAll(entities.toTypedArray())
            onAdded()
        }
    }

    override fun moveUp(bookmarkEntry: BookmarkEntry, onMoved: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){

            //todo - this method is broken,
            //is it?
            val prev = db.bookmarks().getBookmark(bookmarkEntry.index -1)
            val target = db.bookmarks().getBookmark(bookmarkEntry.index)

            db.bookmarks().updateUIIndex(prev.uid, bookmarkEntry.index)
            db.bookmarks().updateUIIndex(target.uid, bookmarkEntry.index - 1)
            onMoved()
        }
    }

    override fun moveDown(bookmarkEntry: BookmarkEntry, onMoved: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val next = db.bookmarks().getBookmark(bookmarkEntry.index + 1)
            val target = db.bookmarks().getBookmark(bookmarkEntry.index)

            db.bookmarks().updateUIIndex(next.uid, bookmarkEntry.index)
            db.bookmarks().updateUIIndex(target.uid, bookmarkEntry.index + 1)
            onMoved()
        }
    }

    override fun update(bookmarkEntry: BookmarkEntry, label: String?, uri: String?, onUpdate: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            db.bookmarks().updateContent(bookmarkEntry.uid, label ?: "", uri ?: "")
            onUpdate()
        }
    }

    override fun delete(bookmarkEntry: BookmarkEntry, onDelete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val entity = db.bookmarks().getBookmark(bookmarkEntry.index)
            db.bookmarks().delete(entity)
            onDelete()
        }
    }
}