package corewala.buran.ui.content_image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.PopupMenu
import kotlinx.android.synthetic.main.dialog_content_image.view.*
import corewala.buran.R
import corewala.buran.io.GemState
import java.io.FileOutputStream

object ImageDialog {

    fun show(context: Context, state: GemState.ResponseImage, onDownloadRequest: (state: GemState.ResponseImage) -> Unit){
        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_content_image, null)
        dialog.setContentView(view)

        view.image_view.setImageURI(state.cacheUri)

        view.close_image_content_dialog.setOnClickListener {
            dialog.dismiss()
        }

        view.image_overflow.setOnClickListener {
            val overflowMenu = PopupMenu(context, view.image_overflow)
            val inflater: MenuInflater = overflowMenu.menuInflater
            inflater.inflate(R.menu.image_overflow_menu, overflowMenu.menu)
            overflowMenu.setOnMenuItemClickListener { menuItem ->
                if(menuItem.itemId == R.id.image_overflow_save_image){
                    onDownloadRequest(state)
                }
                true
            }

            overflowMenu.show()
        }

        dialog.show()
    }

    /**
     *
     * Save bitmap using Storage Access Framework Uri
     * @param bitmap
     * @param uri - must be a SAF Uri
     * @param onComplete
     */
    fun publicExport(context: Context, bitmap: Bitmap?, uri: Uri, onComplete: (uri: Uri) -> Unit) {
        context.contentResolver.openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).use { outputStream ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            bitmap?.recycle()
            onComplete(uri)
        }
    }
}