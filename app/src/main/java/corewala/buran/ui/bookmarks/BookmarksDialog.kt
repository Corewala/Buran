package corewala.buran.ui.bookmarks

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_bookmarks.view.*
import corewala.buran.R
import corewala.buran.io.database.bookmarks.BookmarkEntry
import corewala.buran.io.database.bookmarks.BookmarksDatasource
import corewala.buran.ui.CREATE_BOOKMARK_EXPORT_FILE_REQ
import corewala.buran.ui.CREATE_BOOKMARK_IMPORT_FILE_REQ
import corewala.visible
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.android.synthetic.main.dialog_bookmarks.view.close_tab_dialog
import kotlinx.android.synthetic.main.dialog_history.view.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URI


class BookmarksDialog(
    context: Activity,
    private val bookmarkDatasource: BookmarksDatasource,
    onBookmark: (bookmarkEntry: BookmarkEntry) -> Unit
): AppCompatDialog(context, R.style.FSDialog) {

    var bookmarksAdapter: BookmarksAdapter

    var view: View = View.inflate(context, R.layout.dialog_bookmarks, null)

    init {

        setContentView(view)

        view.close_tab_dialog.setOnClickListener {
            dismiss()
        }

        view.bookmark_overflow.setOnClickListener { menu ->
            val popup = PopupMenu(view.context, view.bookmark_overflow)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.history_overflow_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                if(menuItem.itemId == R.id.menu_action_import_bookmarks){
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.type = "application/json"
                    context.startActivityForResult(intent, CREATE_BOOKMARK_IMPORT_FILE_REQ)
                }else if(menuItem.itemId == R.id.menu_action_export_bookmarks){
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "application/json"
                    intent.putExtra(Intent.EXTRA_TITLE, "buran_bookmarks.json")
                    context.startActivityForResult(intent, CREATE_BOOKMARK_EXPORT_FILE_REQ)
                }
                true
            }
            MenuCompat.setGroupDividerEnabled(popup.menu, true)
            popup.show()
        }



        //None as yet
        /*
        view.bookmarks_toolbar.inflateMenu(R.menu.add_bookmarks)
        view.bookmarks_toolbar.setOnMenuItemClickListener { _ ->
            true
        }

         */

        view.bookmarks_recycler.layoutManager = LinearLayoutManager(context)

        bookmarksAdapter = BookmarksAdapter({ bookmark ->
            //onBookmark
            onBookmark(bookmark)
            dismiss()

        }){ view, bookmark, isFirst, isLast ->
            //onOverflow
            val bookmarkOverflow = PopupMenu(context, view)

            bookmarkOverflow.inflate(R.menu.menu_bookmark)

            if(isFirst) bookmarkOverflow.menu.removeItem(R.id.menu_bookmark_move_up)
            if(isLast) bookmarkOverflow.menu.removeItem(R.id.menu_bookmark_move_down)

            bookmarkOverflow.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId){
                    R.id.menu_bookmark_edit -> edit(bookmark)
                    R.id.menu_bookmark_delete -> delete(bookmark)
                    R.id.menu_bookmark_move_up -> moveUp(bookmark)
                    R.id.menu_bookmark_move_down -> moveDown(bookmark)
                }
                true
            }

            bookmarkOverflow.show()
        }

        view.bookmarks_recycler.adapter = bookmarksAdapter

        bookmarkDatasource.get { bookmarks ->

            Handler(Looper.getMainLooper()).post {
                when {
                    /*
                    bookmarks.isEmpty() -> view.bookmarks_empty_layout.visible(true)

                     */
                    else -> bookmarksAdapter.update(bookmarks)
                }
            }
        }
    }

    private fun edit(bookmarkEntry: BookmarkEntry){
        BookmarkDialog(
            context,
            BookmarkDialog.mode_edit,
            null,
            bookmarkEntry.uri.toString(),
            bookmarkEntry.label
        ){ label, uri ->
            bookmarkDatasource.update(bookmarkEntry, label, uri){
                bookmarkDatasource.get { bookmarks ->
                    Handler(Looper.getMainLooper()).post {
                        bookmarksAdapter.update(bookmarks)
                    }
                }
            }
        }.show()
    }

    /**
     *
     * Bookmark isn't actually deleted from the DB until the Snackbar disappears. Which is nice.
     *
     */
    private fun delete(bookmarkEntry: BookmarkEntry){
        //OnDelete
        bookmarksAdapter.hide(bookmarkEntry)
        Snackbar.make(view, "Deleted ${bookmarkEntry.label}", Snackbar.LENGTH_SHORT).addCallback(
            object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) = when (event) {
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION -> bookmarksAdapter.show(
                        bookmarkEntry
                    )
                    else -> bookmarkDatasource.delete(bookmarkEntry) {
                        Handler(Looper.getMainLooper()).post {
                            bookmarksAdapter.remove(bookmarkEntry)
                        }
                    }
                }
            }).setAction("Undo"){
            //Action listener unused
        }.show()
    }

    private fun moveUp(bookmarkEntry: BookmarkEntry){
        bookmarkDatasource.moveUp(bookmarkEntry){
            bookmarkDatasource.get { bookmarks ->
                Handler(Looper.getMainLooper()).post {
                    bookmarksAdapter.update(bookmarks)
                }
            }
        }
    }

    private fun moveDown(bookmarkEntry: BookmarkEntry){
        bookmarkDatasource.moveDown(bookmarkEntry){
            bookmarkDatasource.get { bookmarks ->
                Handler(Looper.getMainLooper()).post {
                    bookmarksAdapter.update(bookmarks)
                }
            }
        }
    }

    fun bookmarksExportFileReady(uri: Uri){
        val model = BookmarksViewModel()
        model.initialise(bookmarkDatasource){
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Bookmarks Exported", Toast.LENGTH_SHORT).show()
            }
        }
        model.exportBookmarks(context.contentResolver, uri)
    }

    fun bookmarksImportFileReady(uri: Uri){
        context.contentResolver.openInputStream(uri).use{ inputStream ->
            InputStreamReader(inputStream).use { streamReader ->
                BufferedReader(streamReader).use { bufferedReader ->
                    val sb = StringBuilder()
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        sb.append(line).append('\n')
                    }
                    val bookmarksRawJson = sb.toString()
                    val bookmarksJson = JSONObject(bookmarksRawJson)
                    val bookmarks = bookmarksJson.getJSONArray("bookmarks")
                    val bookmarkEntries = arrayListOf<BookmarkEntry>()

                    var skipped = 0
                    var added = 0

                    repeat(bookmarks.length()){ index ->
                        val bookmark = bookmarks.getJSONObject(index)
                        val bookmarkLabel = bookmark.getString("label")
                        val bookmarkUri = bookmark.getString("uri")
                        println("Importing bookmark: $bookmarkLabel : $uri")
                        val existing = bookmarksAdapter.bookmarks.filter {  entry ->
                            entry.uri.toString() == bookmarkUri
                        }
                        when {
                            existing.isNotEmpty() -> skipped++
                            else -> {
                                added++
                                bookmarkEntries.add(BookmarkEntry(-1, bookmarkLabel, URI.create(bookmarkUri), index))
                            }
                        }
                    }

                    bookmarkDatasource.add(bookmarkEntries.toTypedArray()){
                        bookmarkDatasource.get { bookmarks ->
                            Handler(Looper.getMainLooper()).post {

                                view.bookmarks_empty_layout.visible(false)

                                bookmarksAdapter.update(bookmarks)
                                when {
                                    skipped > 0 -> {
                                        Toast.makeText(context, "$added bookmarks imported ($skipped duplicates)", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> Toast.makeText(context, "$added bookmarks imported", Toast.LENGTH_SHORT).show()
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}