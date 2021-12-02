package corewala.buran.ui.bookmarks

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.fragment_bookmark_dialog.view.*
import corewala.buran.R
import corewala.buran.io.database.bookmarks.BookmarkEntry
import corewala.buran.io.database.bookmarks.BookmarksDatasource
import java.net.URI

class BookmarkDialog(
    context: Context,
    private val mode: Int,
    private val bookmarkDatasource: BookmarksDatasource?,
    val uri: String,
    val name: String,
    onDismiss: (label: String?, uri: String?) -> Unit) : AppCompatDialog(context, R.style.FSDialog) {

    companion object{
        const val mode_new = 0
        const val mode_edit = 1
    }

    init {
        val view = View.inflate(context, R.layout.fragment_bookmark_dialog, null)

        setContentView(view)

        view.bookmark_toolbar.setNavigationIcon(R.drawable.vector_close)
        view.bookmark_toolbar.setNavigationOnClickListener {
            onDismiss(null, null)
            dismiss()
        }

        view.bookmark_name.setText(name)
        view.bookmark_uri.setText(uri)

        view.bookmark_toolbar.inflateMenu(R.menu.add_bookmark)
        view.bookmark_toolbar.setOnMenuItemClickListener {menuItem ->
            if(menuItem.itemId == R.id.menu_action_save_bookmark){

                if(mode == mode_new) {
                    //Determine index:
                    //todo - this is expensive, just get last item, limit1?
                    bookmarkDatasource?.get { allBookmarks ->

                        val index = when {
                            allBookmarks.isEmpty() -> 0
                            else -> allBookmarks.last().index + 1
                        }

                        bookmarkDatasource.add(

                            BookmarkEntry(
                                uid = -1,
                                label = view.bookmark_name.text.toString(),
                                uri = URI.create(view.bookmark_uri.text.toString()),
                                index = index
                            )
                        ) {
                            Handler(Looper.getMainLooper()).post {
                                onDismiss(null, null)
                                dismiss()
                            }
                        }
                    }
                }else if(mode == mode_edit){
                    onDismiss(
                        view.bookmark_name.text.toString(),
                        view.bookmark_uri.text.toString())
                    dismiss()
                }
            }

            true

        }
    }
}