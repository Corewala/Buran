package corewala.buran.ui.modals_menus.history

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import corewala.buran.OmniTerm
import kotlinx.android.synthetic.main.dialog_history.view.*
import corewala.buran.R
import corewala.buran.io.database.history.BuranHistory

object HistoryDialog {
    fun show(context: Context, history: BuranHistory, omniTerm: OmniTerm, onHistoryItem: (address: String) -> Unit){

        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_history, null)
        dialog.setContentView(view)

        view.history_toolbar.setNavigationIcon(R.drawable.vector_close)
        view.history_toolbar.setNavigationOnClickListener {
            dialog.dismiss()
        }

        view.history_toolbar.menu.forEach { menu ->
            menu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.menu_action_clear_history -> {
                        history.clear {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, context.getString(R.string.history_cleared), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    R.id.menu_action_clear_runtime_cache -> {
                        omniTerm.clearCache()
                        Toast.makeText(context, context.getString(R.string.runtime_cache_cleared), Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                    }
                }
                true
            }
        }

        view.history_toolbar.setOnMenuItemClickListener { _ ->
            true
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