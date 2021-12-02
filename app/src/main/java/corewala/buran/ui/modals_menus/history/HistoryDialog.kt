package corewala.buran.ui.modals_menus.history

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_about.view.close_tab_dialog
import kotlinx.android.synthetic.main.dialog_history.view.*
import corewala.buran.R
import corewala.buran.io.database.history.BuranHistory

object HistoryDialog {
    fun show(context: Context, history: BuranHistory, onHistoryItem: (address: String) -> Unit){
        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_history, null)
        dialog.setContentView(view)

        view.close_tab_dialog.setOnClickListener {
            dialog.dismiss()
        }

        view.history_overflow.setOnClickListener {
            val popup = PopupMenu(view.context, view.history_overflow)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.history_overflow_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                if(menuItem.itemId == R.id.history_overflow_clear_history){
                    history.clear {
                        Handler(Looper.getMainLooper()).post {
                            dialog.dismiss()
                            Toast.makeText(context, context.getString(R.string.history_cleared), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else if(menuItem.itemId == R.id.history_overflow_clear_runtime_cache){
                    dialog.dismiss()
                    Toast.makeText(context, context.getString(R.string.runtime_cahce_cleared), Toast.LENGTH_SHORT).show()
                }
                true
            }
            MenuCompat.setGroupDividerEnabled(popup.menu, true)
            popup.show()
        }

        view.history_recycler.layoutManager = LinearLayoutManager(context)

        history.get { history ->
           Handler(Looper.getMainLooper()).post {
               view.history_recycler.adapter = HistoryAdapter(history) { entry ->
                   onHistoryItem(entry.uri.toString())
                   dialog.dismiss()
               }

               dialog.show()
           }
        }
    }
}