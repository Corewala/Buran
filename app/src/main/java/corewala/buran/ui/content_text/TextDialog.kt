package corewala.buran.ui.content_text

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.dialog_content_text.view.*
import corewala.buran.R
import corewala.buran.io.GemState

object TextDialog {

    fun show(context: Context, state: GemState.ResponseText){
        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_content_text, null)
        dialog.setContentView(view)

        view.text_content.text = state.content

        view.close_text_content_dialog.setOnClickListener {
            dialog.dismiss()
        }


        dialog.show()
    }
}