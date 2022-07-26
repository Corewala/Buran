package corewala.buran.ui.gemtext_adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import corewala.buran.R
import corewala.endsWithImage
import corewala.visible
import kotlinx.android.synthetic.main.gemtext_code_block.view.*
import kotlinx.android.synthetic.main.gemtext_image_link.view.*
import kotlinx.android.synthetic.main.gemtext_link.view.gemtext_text_link
import kotlinx.android.synthetic.main.gemtext_quote.view.*
import kotlinx.android.synthetic.main.gemtext_text.view.*
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
            is GmiViewHolder.Text -> {
                when {
                    useAttentionGuides -> holder.itemView.gemtext_text_textview.text = getAttentionGuideText(line)
                    else -> holder.itemView.gemtext_text_textview.text = line
                }
            }
            is GmiViewHolder.Code -> {
                if(line.startsWith("```<|ALT|>")){
                    holder.itemView.gemtext_text_monospace_textview.text = line.substring(line.indexOf("</|ALT>") + 7)
                }else{
                    holder.itemView.gemtext_text_monospace_textview.text = line.substring(3)
                }
            }
            is GmiViewHolder.Quote -> {
                when {
                    useAttentionGuides -> holder.itemView.gemtext_quote_textview.text = getAttentionGuideText(line.substring(1).trim())
                    else -> holder.itemView.gemtext_quote_textview.text = line.substring(1).trim()
                }
            }
            is GmiViewHolder.H1 -> {
                when {
                    line.length > 2 -> holder.itemView.gemtext_text_textview.text = line.substring(1).trim()
                    else -> holder.itemView.gemtext_text_textview.text = ""
                }
            }
            is GmiViewHolder.H2 -> {
                when {
                    line.length > 3 -> holder.itemView.gemtext_text_textview.text = line.substring(2).trim()
                    else -> holder.itemView.gemtext_text_textview.text = ""
                }
            }
            is GmiViewHolder.H3 -> {
                when {
                    line.length > 4 -> holder.itemView.gemtext_text_textview.text = line.substring(3).trim()
                    else -> holder.itemView.gemtext_text_textview.text = ""
                }
            }
            is GmiViewHolder.ListItem -> {
                when {
                    useAttentionGuides -> holder.itemView.gemtext_text_textview.text = getAttentionGuideText("• ${line.substring(1)}".trim())
                    else -> holder.itemView.gemtext_text_textview.text = "• ${line.substring(1)}".trim()
                }
            }
            is GmiViewHolder.Link -> {
                val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
                var linkName = linkParts[0]

                if(linkParts.size > 1) linkName = linkParts[1]

                val displayText = linkName

                when {
                    showLinkButtons -> {
                        holder.itemView.gemtext_text_link.visible(false)
                        holder.itemView.gemtext_link_button.visible(true)
                        holder.itemView.gemtext_link_button.text = displayText
                    } else -> {
                        holder.itemView.gemtext_link_button.visible(false)
                        holder.itemView.gemtext_text_link.visible(true)
                        holder.itemView.gemtext_text_link.text = displayText
                        holder.itemView.gemtext_text_link.paint.isUnderlineText = true
                    }
                }

                when {
                    showInlineIcons && linkParts.first().startsWith("http") -> {
                        holder.itemView.gemtext_text_link.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_open_browser, 0)
                        holder.itemView.gemtext_link_button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_open_browser, 0)
                    }
                    else -> {
                        holder.itemView.gemtext_text_link.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        holder.itemView.gemtext_link_button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }

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
                holder.itemView.gemtext_link_button.setOnClickListener {
                    val uri = getUri(lines[holder.adapterPosition])
                    println("User clicked link: $uri")
                    onLink(uri, false, holder.adapterPosition)
                }
                holder.itemView.gemtext_link_button.setOnLongClickListener {
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

                when {
                    showLinkButtons -> {
                        holder.itemView.gemtext_text_link.visible(false)
                        holder.itemView.gemtext_link_button.visible(true)
                        holder.itemView.gemtext_link_button.text = displayText
                    } else -> {
                    holder.itemView.gemtext_link_button.visible(false)
                    holder.itemView.gemtext_text_link.visible(true)
                    holder.itemView.gemtext_text_link.text = displayText
                    holder.itemView.gemtext_text_link.paint.isUnderlineText = true
                }
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
                holder.itemView.gemtext_link_button.setOnClickListener {
                    val uri = getUri(lines[holder.adapterPosition])
                    println("User clicked link: $uri")
                    onLink(uri, false, holder.adapterPosition)
                }
                holder.itemView.gemtext_link_button.setOnLongClickListener {
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

                if(("://" !in getLink(lines[holder.adapterPosition])) and showInlineImages){
                    when {
                        inlineImages.containsKey(position) -> {
                            holder.itemView.rounded_image_frame.visible(true)
                            holder.itemView.gemtext_inline_image.setImageURI(inlineImages[position])
                        }
                        else -> {
                            val uri = getUri(lines[holder.adapterPosition])
                            println("Inline image rendered: $uri")
                            inlineImage(uri, holder.adapterPosition)
                        }
                    }
                }else{
                    holder.itemView.rounded_image_frame.visible(false)
                }

                when {
                    showInlineIcons -> {
                        holder.itemView.gemtext_text_link.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_photo, 0)
                        holder.itemView.gemtext_link_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_photo, 0)
                    }
                    else -> {
                        holder.itemView.gemtext_text_link.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        holder.itemView.gemtext_link_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
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

    private fun getAttentionGuideText(text: String): SpannableStringBuilder {
        val wordList = text.split(" ")

        val attentionGuideText = SpannableStringBuilder()
        for(word in wordList){
            val wordComponents = word.split("-")

            for(component in wordComponents) {
                val joiner = if((wordComponents.size > 1) and (wordComponents.indexOf(component) != wordComponents.size - 1)){
                    "-"
                }else{
                    " "
                }
                if (component.length > 1) {
                    if (component.first().isLetterOrDigit()) {
                        val index = component.length / 2
                        attentionGuideText
                            .bold { append(component.substring(0, index)) }
                            .append("${component.substring(index)}$joiner")
                    } else {
                        var offset = 1

                        if (component.length - offset > 1) {
                            while ((component.length - offset > 1) and !component.substring(offset).first().isLetterOrDigit()) {
                                offset += 1
                            }
                            val index = (component.length - offset) / 2
                            attentionGuideText
                                .append(component.substring(0, offset))
                                .bold { append(component.substring(offset, index + offset)) }
                                .append("${component.substring(index + offset)}$joiner")
                        }else{
                            attentionGuideText.append("$component$joiner")
                        }
                    }
                } else {
                    attentionGuideText.append("$component$joiner")
                }
            }
        }
        return attentionGuideText
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

    override fun linkButtons(visible: Boolean){
        this.showLinkButtons = visible
        notifyDataSetChanged()
    }

    override fun attentionGuides(enabled: Boolean){
        this.useAttentionGuides = enabled
        notifyDataSetChanged()
    }

    override fun inlineImages(visible: Boolean){
        this.showInlineImages = visible
        notifyDataSetChanged()
    }
}