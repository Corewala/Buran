package corewala.buran.ui.content_image

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach
import kotlinx.android.synthetic.main.dialog_content_image.view.*
import corewala.buran.R
import corewala.buran.io.GemState
import corewala.buran.ui.CREATE_BOOKMARK_EXPORT_FILE_REQ
import corewala.buran.ui.CREATE_BOOKMARK_IMPORT_FILE_REQ
import kotlinx.android.synthetic.main.dialog_bookmarks.view.*
import kotlinx.android.synthetic.main.dialog_content_text.view.*
import java.io.FileOutputStream

object ImageDialog {

    fun show(context: Context, state: GemState.ResponseImage, onDownloadRequest: (state: GemState.ResponseImage) -> Unit){

        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_content_image, null)
        dialog.setContentView(view)

        view.image_view.setImageURI(state.cacheUri)

        view.image_toolbar.setNavigationIcon(R.drawable.vector_close)
        view.image_toolbar.setNavigationOnClickListener {
            dialog.dismiss()
        }

        view.image_toolbar.menu.forEach { menu ->
            menu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.image_overflow_save_image -> {
                        onDownloadRequest(state)
                    }
                    else -> {

                    }
                }
                true
            }
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