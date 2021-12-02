package corewala.buran.ui.bookmarks

import android.content.ContentResolver
import android.net.Uri
import corewala.buran.io.database.bookmarks.BookmarksDatasource
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.PrintStream

/**
 *
 * Pseudo viewmodel for now until I can find time to refactor the entire dialog - putting new functionality here
 *
 */
class BookmarksViewModel {

    lateinit var datasource: BookmarksDatasource

    var onExport: () -> Unit = {}

    fun initialise(datasource: BookmarksDatasource, onExport: () -> Unit){
        this.datasource = datasource
        this.onExport = onExport
    }


    fun exportBookmarks(contentResolver: ContentResolver, uri: Uri){
        datasource.get { bookmarks ->
            val json = JSONObject()
            val bookmarksJson = JSONArray()


            bookmarks.forEach { entry ->
                val bookmarkJson = JSONObject()
                bookmarkJson.put("label", entry.label)
                bookmarkJson.put("uri", entry.uri)
                bookmarksJson.put(bookmarkJson)
            }

            json.put("bookmarks", bookmarksJson)

            val bookmarks = json.toString(2)
            println("Bookmarks json to export: $bookmarks")

            contentResolver.openFileDescriptor(uri, "w")?.use { fileDescriptor ->
                FileOutputStream(fileDescriptor.fileDescriptor).use { os ->
                    PrintStream(os).use{
                        it.print(bookmarks)
                        it.flush()
                        it.close()
                        os.close()
                        onExport.invoke()
                    }
                }
            }
        }
    }
}