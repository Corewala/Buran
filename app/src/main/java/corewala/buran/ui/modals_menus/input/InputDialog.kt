package corewala.buran.ui.modals_menus.input

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.dialog_input_query.view.*
import corewala.buran.R
import corewala.buran.io.GemState
import java.net.URLEncoder

object InputDialog {

    fun show(context: Context, state: GemState.ResponseInput, onQuery: (queryAddress: String) -> Unit) {
        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_input_query, null)
        dialog.setContentView(view)

        view.close_input_query_dialog.setOnClickListener {
            dialog.dismiss()
        }

        view.query_text.text = state.header.meta

        view.query_submit_button.setOnClickListener {
            val encoded = URLEncoder.encode(view.query_input.text.toString(), "UTF-8")
            onQuery("${state.uri}?$encoded")
            dialog.dismiss()
        }

        dialog.show()
    }
}