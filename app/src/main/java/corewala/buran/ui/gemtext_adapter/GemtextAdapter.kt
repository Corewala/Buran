package corewala.buran.ui.gemtext_adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.gemtext_code_block.view.*
import kotlinx.android.synthetic.main.gemtext_image_link.view.*
import kotlinx.android.synthetic.main.gemtext_link.view.gemtext_text_link
import kotlinx.android.synthetic.main.gemtext_text.view.*
import corewala.buran.R
import corewala.endsWithImage
import corewala.visible
import java.net.URI

class GemtextAdapter(
    onLink: (link: URI, longTap: Boolean, adapterPosition: Int) -> Unit,
    inlineImage: (link: URI, adapterPosition: Int) -> Unit
): AbstractGemtextAdapter(onLink, inlineImage) {

    private var lines = mutableListOf<String>()
    private var inlineImages = HashMap<Int, Uri>()

    private val typeText = 0
    private val typeH1 = 1
    private val typeH2 = 2
    private val typeH3 = 3
    private val typeListItem = 4
    private val typeImageLink = 5
    private val typeLink = 6
    private val typeCodeBlock = 7
    private val typeQuote = 8

    override fun render(lines: List<String>){
        this.inlineImages.clear()
        this.lines.clear()
        this.lines.addAll(lines)
        notifyDataSetChanged()
    }

    private fun inflate(parent: ViewGroup, layout: Int): View{
        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GmiViewHolder {
        return when(viewType){
            typeText -> GmiViewHolder.Text(inflate(parent, R.layout.gemtext_text))
            typeH1 -> GmiViewHolder.H1(inflate(parent, R.layout.gemtext_h1))
            typeH2 -> GmiViewHolder.H2(inflate(parent, R.layout.gemtext_h2))
            typeH3 -> GmiViewHolder.H3(inflate(parent, R.layout.gemtext_h3))
            typeListItem -> GmiViewHolder.ListItem(inflate(parent, R.layout.gemtext_text))
            typeImageLink -> GmiViewHolder.ImageLink(inflate(parent, R.layout.gemtext_image_link))
            typeLink -> GmiViewHolder.Link(inflate(parent, R.layout.gemtext_link))
            typeCodeBlock-> GmiViewHolder.Code(inflate(parent, R.layout.gemtext_code_block))
            typeQuote -> GmiViewHolder.Quote(inflate(parent, R.layout.gemtext_quote))
            else -> GmiViewHolder.Text(inflate(parent, R.layout.gemtext_text))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val line = lines[position]
        return when {
            line.startsWith("```") -> typeCodeBlock
            line.startsWith("###") -> typeH3
            line.startsWith("##") -> typeH2
            line.startsWith("#") -> typeH1
            line.startsWith("*") -> typeListItem
            line.startsWith("=>") && getLink(line).endsWithImage() -> typeImageLink
            line.startsWith("=>") -> typeLink
            line.startsWith(">") -> typeQuote
            else -> typeText
        }
    }

    override fun getItemCount(): Int = lines.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GmiViewHolder, position: Int) {
        val line = lines[position]

        when(holder){
            is GmiViewHolder.Text -> holder.itemView.gemtext_text_textview.text = line
            is GmiViewHolder.Code -> {

                var altText: String? = null

                if(line.startsWith("```<|ALT|>")){
                    //there's alt text: "```<|ALT|>$alt</|ALT>"
                    altText = line.substring(10, line.indexOf("</|ALT>"))
                    holder.itemView.gemtext_text_monospace_textview.text = line.substring(line.indexOf("</|ALT>") + 7)
                }else{
                    holder.itemView.gemtext_text_monospace_textview.text = line.substring(3)
                }

                if(hideCodeBlocks){
                    holder.itemView.show_code_block.setText(R.string.show_code)//reset for recycling
                    altText?.let{
                        holder.itemView.show_code_block.append(": $altText")
                    }
                    holder.itemView.show_code_block.visible(true)
                    holder.itemView.show_code_block.paint.isUnderlineText = true
                    holder.itemView.show_code_block.setOnClickListener {
                        setupCodeBlockToggle(holder, altText)
                    }
                    holder.itemView.gemtext_text_monospace_textview.visible(false)

                    when {
                        showInlineIcons -> holder.itemView.show_code_block.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_code, 0)
                        else -> holder.itemView.show_code_block.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }else{
                    holder.itemView.show_code_block.visible(false)
                    holder.itemView.gemtext_text_monospace_textview.visible(true)
                }
            }
            is GmiViewHolder.Quote -> holder.itemView.gemtext_text_monospace_textview.text = line.substring(1).trim()
            is GmiViewHolder.H1 -> {
                when {
                    line.length > 2 -> holder.itemView.gemtext_text_textview.text = line.substring(2).trim()
                    else -> holder.itemView.gemtext_text_textview.text = ""
                }
            }
            is GmiViewHolder.H2 -> {
                when {
                    line.length > 3 -> holder.itemView.gemtext_text_textview.text = line.substring(3).trim()
                    else -> holder.itemView.gemtext_text_textview.text = ""
                }
            }
            is GmiViewHolder.H3 -> {
                when {
                    line.length > 4 -> holder.itemView.gemtext_text_textview.text = line.substring(4).trim()
                    else -> holder.itemView.gemtext_text_textview.text = ""
                }
            }
            is GmiViewHolder.ListItem -> holder.itemView.gemtext_text_textview.text = "• ${line.substring(1)}".trim()
            is GmiViewHolder.Link -> {
                val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
                var linkName = linkParts[0]

                if(linkParts.size > 1) linkName = linkParts[1]

                val displayText = linkName
                holder.itemView.gemtext_text_link.text = displayText
                holder.itemView.gemtext_text_link.paint.isUnderlineText = true

                when {
                    showInlineIcons && linkParts.first().startsWith("http") -> holder.itemView.gemtext_text_link.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_open_browser, 0)
                    else -> holder.itemView.gemtext_text_link.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }

                holder.itemView.gemtext_text_link.setOnClickListener {
                    val uri = getUri(lines[holder.adapterPosition])
                    println("User clicked link: $uri")
                    onLink(uri, false, holder.adapterPosition)

                }
                holder.itemView.gemtext_text_link.setOnLongClickListener {
                    val uri = getUri(lines[holder.adapterPosition])
                    println("User long-clicked link: $uri")
                    onLink(uri, true, holder.adapterPosition)
                    true
                }
            }
            is GmiViewHolder.ImageLink -> {

                val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
                var linkName = linkParts[0]

                if(linkParts.size > 1) linkName = linkParts[1]

                val displayText = linkName
                holder.itemView.gemtext_text_link.text = displayText
                holder.itemView.gemtext_text_link.paint.isUnderlineText = true
                holder.itemView.gemtext_text_link.setOnClickListener {
                    val uri = getUri(lines[holder.adapterPosition])
                    println("User clicked link: $uri")
                    onLink(uri, false, holder.adapterPosition)
                }
                holder.itemView.gemtext_text_link.setOnLongClickListener {
                    val uri = getUri(lines[holder.adapterPosition])
                    println("User long-clicked link: $uri")
                    onLink(uri, true, holder.adapterPosition)
                    true
                }
                holder.itemView.gemtext_inline_image.setOnClickListener {
                    val uri = getUri(lines[holder.adapterPosition])
                    println("User clicked image: $uri")
                    onLink(uri, false, holder.adapterPosition)
                }
                holder.itemView.gemtext_inline_image.setOnLongClickListener {
                    val uri = getUri(lines[holder.adapterPosition])
                    println("User long-clicked image: $uri")
                    onLink(uri, true, holder.adapterPosition)
                    true
                }

                if(getLink(lines[holder.adapterPosition]).first() == '/'){
                    when {
                        inlineImages.containsKey(position) -> {
                            holder.itemView.rounded_image_frame.visible(true)
                            holder.itemView.gemtext_inline_image.setImageURI(inlineImages[position])
                        }
                        else -> {
                            if (showInlineImages){
                                val uri = getUri(lines[holder.adapterPosition])
                                println("Inline image rendered: $uri")
                                inlineImage(uri, holder.adapterPosition)
                            }
                        }
                    }
                    when {
                        showInlineImages -> holder.itemView.rounded_image_frame.visible(true)
                        else -> holder.itemView.rounded_image_frame.visible(false)
                    }
                }

                when {
                    showInlineIcons -> holder.itemView.gemtext_text_link.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_photo, 0)
                    else -> holder.itemView.gemtext_text_link.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
        }
    }

    private fun setupCodeBlockToggle(holder: GmiViewHolder.Code, altText: String?) {
        //val adapterPosition = holder.adapterPosition
        when {
            holder.itemView.gemtext_text_monospace_textview.isVisible -> {
                holder.itemView.show_code_block.setText(R.string.show_code)
                holder.itemView.gemtext_text_monospace_textview.visible(false)
                altText?.let{
                    holder.itemView.show_code_block.append(": $altText")
                }
            }
            else -> {
                holder.itemView.show_code_block.setText(R.string.hide_code)
                holder.itemView.gemtext_text_monospace_textview.visible(true)
                altText?.let{
                    holder.itemView.show_code_block.append(": $altText")
                }
            }
        }
    }

    private fun getLink(line: String): String{
        val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
        return linkParts[0]
    }

    private fun getUri(linkLine: String): URI{
        val linkParts = linkLine.substring(2).trim().split("\\s+".toRegex(), 2)
        return URI.create(linkParts.first())
    }

    override fun inferTitle(): String? {
        lines.forEach { line ->
            if(line.startsWith("#")) return line.replace("#", "").trim()
        }

        return null
    }

    override fun loadImage(position: Int, cacheUri: Uri){
        inlineImages[position] = cacheUri
        notifyItemChanged(position)
    }

    override fun inlineIcons(visible: Boolean){
        this.showInlineIcons = visible
        notifyDataSetChanged()
    }

    override fun inlineImages(visible: Boolean){
        this.showInlineImages = visible
        notifyDataSetChanged()
    }

    override fun hideCodeBlocks(hideCodeBlocks: Boolean) {
        this.hideCodeBlocks = hideCodeBlocks
        notifyDataSetChanged()
    }
}