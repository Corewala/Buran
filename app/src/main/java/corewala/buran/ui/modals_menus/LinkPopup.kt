package corewala.buran.ui.modals_menus

import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.PopupMenu
import corewala.buran.R
import corewala.endsWithImage
import corewala.isWeb
import java.net.URI

object LinkPopup {

    fun show(view: View?, uri: URI, onMenuOption: (menuId: Int) -> Unit){
        if(view != null) {

            val popup = PopupMenu(view.context, view)
            val inflater: MenuInflater = popup.menuInflater

            val uriStr = uri.toString()

            when {
                uriStr.endsWithImage() && !uriStr.isWeb() -> inflater.inflate(R.menu.image_link_menu, popup.menu)
                else -> inflater.inflate(R.menu.link_menu, popup.menu)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                onMenuOption(menuItem.itemId)
                true
            }

            popup.show()
        }
    }
}