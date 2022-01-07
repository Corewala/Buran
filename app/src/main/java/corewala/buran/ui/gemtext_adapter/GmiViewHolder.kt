package corewala.buran.ui.gemtext_adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

sealed class GmiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    class Text(itemView: View): GmiViewHolder(itemView)
    class H1(itemView: View): GmiViewHolder(itemView)
    class H2(itemView: View): GmiViewHolder(itemView)
    class H3(itemView: View): GmiViewHolder(itemView)
    class ListItem(itemView: View): GmiViewHolder(itemView)
    class ImageLink(itemView: View): GmiViewHolder(itemView)
    class Link(itemView: View): GmiViewHolder(itemView)
    class Code(itemView: View): GmiViewHolder(itemView)
    class Quote(itemView: View): GmiViewHolder(itemView)
}