package corewala.buran.ui.modals_menus.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import corewala.buran.R
import java.lang.StringBuilder
import java.security.SecureRandom
import java.security.Security
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

object AboutDialog {

    fun show(context: Context){
        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_about, null)
        dialog.setContentView(view)

        view.close_tab_dialog.setOnClickListener {
            dialog.dismiss()
        }

        view.source_button.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://github.com/Corewala/Buran")
            })
        }

        dialog.show()
    }

}