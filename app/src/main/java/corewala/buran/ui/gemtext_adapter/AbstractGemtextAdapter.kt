package corewala.buran.ui.gemtext_adapter

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import java.net.URI

abstract class AbstractGemtextAdapter(
    val onLink: (link: URI, longTap: Boolean, adapterPosition: Int) -> Unit,
    val inlineImage: (link: URI, adapterPosition: Int) -> Unit
): RecyclerView.Adapter<GmiViewHolder>() {

    var showInlineIcons: Boolean = false
    var hideCodeBlocks: Boolean = false
    var showLinkButtons: Boolean = false
    var showInlineImages: Boolean = false

    abstract fun render(lines: List<String>)
    abstract fun loadImage(position: Int, cacheUri: Uri)
    abstract fun inlineIcons(visible: Boolean)
    abstract fun inlineImages(visible: Boolean)
    abstract fun hideCodeBlocks(hideCodeBlocks: Boolean)
    abstract fun linkButtons(visible: Boolean)

    abstract fun inferTitle(): String?

    companion object{
        fun getAdapter(
            onLink: (link: URI, longTap: Boolean, adapterPosition: Int) -> Unit,
            inlineImage: (link: URI, adapterPosition: Int) -> Unit
        ): AbstractGemtextAdapter {
            return GemtextAdapter(onLink, inlineImage)
        }
    }
}