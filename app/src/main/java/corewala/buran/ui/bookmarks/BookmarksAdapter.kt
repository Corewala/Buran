package corewala.buran.ui.bookmarks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bookmark.view.*
import corewala.buran.R
import corewala.buran.io.database.bookmarks.BookmarkEntry
import corewala.visible

class BookmarksAdapter(val onBookmark: (bookmarkEntry: BookmarkEntry) -> Unit, val onOverflow: (view: View, bookmarkEntry: BookmarkEntry, isFirst: Boolean, isLast: Boolean) -> Unit): RecyclerView.Adapter<BookmarksAdapter.ViewHolder>() {

    val bookmarks = mutableListOf<BookmarkEntry>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun update(bookmarkEntries: List<BookmarkEntry>){
        this.bookmarks.clear()
        this.bookmarks.addAll(bookmarkEntries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.bookmark, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark = bookmarks[position]

        if(bookmark.visible) {
            holder.itemView.visible(true)
            holder.itemView.bookmark_name.text = bookmark.label
            holder.itemView.bookmark_uri.text = bookmark.uri.toString()

            holder.itemView.bookmark_layout.setOnClickListener {
                onBookmark(bookmarks[holder.adapterPosition])
            }

            holder.itemView.bookmark_overflow.setOnClickListener { view ->
                val isFirst = (holder.adapterPosition == 0)
                val isLast = (holder.adapterPosition == bookmarks.size - 1)
                onOverflow(view, bookmarks[holder.adapterPosition], isFirst, isLast)
            }
        }else{
            holder.itemView.visible(false)
        }
    }

    override fun getItemCount(): Int = bookmarks.size

    fun hide(bookmarkEntry: BookmarkEntry) {
        bookmarkEntry.visible = false
        notifyItemChanged(bookmarks.indexOf(bookmarkEntry))
    }

    fun show(bookmarkEntry: BookmarkEntry) {
        bookmarkEntry.visible = true
        notifyItemChanged(bookmarks.indexOf(bookmarkEntry))
    }

    fun remove(bookmarkEntry: BookmarkEntry){
        val index = bookmarks.indexOf(bookmarkEntry)
        bookmarks.remove(bookmarkEntry)
        notifyItemRemoved(index)
    }
}