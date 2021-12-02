package corewala.buran.ui.modals_menus.overflow

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import corewala.buran.R


object OverflowPopup {

    fun show(view: View?, onMenuOption: (menuId: Int) -> Unit){
        if(view != null) {
            val popup = PopupMenu(view.context, view)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.overflow_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                onMenuOption(menuItem.itemId)
                true
            }
            MenuCompat.setGroupDividerEnabled(popup.menu, true)
            //insertMenuItemIcons(view.context, popup)
            popup.show()
        }
    }

    fun insertMenuItemIcons(context: Context, popupMenu: PopupMenu) {
        val menu: Menu = popupMenu.menu
        if (hasIcon(menu)) {
            for (i in 0 until menu.size()) {
                insertMenuItemIcon(context, menu.getItem(i))
            }
        }
    }

    private fun hasIcon(menu: Menu): Boolean {
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).icon != null) return true
        }
        return false
    }

    /**
     * Converts the given MenuItem's title into a Spannable containing both its icon and title.
     */
    private fun insertMenuItemIcon(context: Context, menuItem: MenuItem) {
        val icon: Drawable = menuItem.icon
        val iconSize = context.resources.getDimensionPixelSize(R.dimen.menu_item_icon_size)
        icon.setBounds(0, 0, iconSize, iconSize)
        icon.setTint(Color.WHITE)
        val imageSpan = ImageSpan(icon)
        val ssb = SpannableStringBuilder("    " + menuItem.title)
        ssb.setSpan(imageSpan, 1, 2, 0)
        menuItem.title = ssb
        menuItem.icon = null
    }
}