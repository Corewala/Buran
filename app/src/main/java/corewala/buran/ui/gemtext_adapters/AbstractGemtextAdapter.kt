package corewala.buran.ui.gemtext_adapters

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import java.net.URI

const val GEMTEXT_ADAPTER_DEFAULT = 0
const val GEMTEXT_ADAPTER_LARGE = 1

abstract class AbstractGemtextAdapter(
    val typeId: Int,
    val onLink: (link: URI, longTap: Boolean, adapterPosition: Int) -> Unit
): RecyclerView.Adapter<GmiViewHolder>() {

    var showInlineIcons: Boolean = false
    var hideCodeBlocks: Boolean = false

    abstract fun render(lines: List<String>)
    abstract fun loadImage(position: Int, cacheUri: Uri)
    abstract fun inlineIcons(visible: Boolean)
    abstract fun hideCodeBlocks(hideCodeBlocks: Boolean)

    abstract fun inferTitle(): String?

    companion object{
        fun getDefault(onLink: (link: URI, longTap: Boolean, adapterPosition: Int) -> Unit): AbstractGemtextAdapter {
            return DefaultGemtextAdapter(GEMTEXT_ADAPTER_DEFAULT, onLink)
        }

        fun getLargeGmi(onLink: (link: URI, longTap: Boolean, adapterPosition: Int) -> Unit): AbstractGemtextAdapter {
            return LargeGemtextAdapter(GEMTEXT_ADAPTER_LARGE, onLink)
        }
    }
}