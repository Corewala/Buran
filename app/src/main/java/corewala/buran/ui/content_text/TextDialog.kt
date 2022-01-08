package corewala.buran.ui.content_text

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.dialog_content_text.view.*
import corewala.buran.R
import corewala.buran.io.GemState
import kotlinx.android.synthetic.main.dialog_bookmarks.view.*

object TextDialog {

    fun show(context: Context, state: GemState.ResponseText){
        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_content_text, null)
        dialog.setContentView(view)

        view.text_content.text = state.content

        view.text_toolbar.setNavigationIcon(R.drawable.vector_close)
        view.text_toolbar.setNavigationOnClickListener {
            dialog.dismiss()
        }



        dialog.show()
    }
}