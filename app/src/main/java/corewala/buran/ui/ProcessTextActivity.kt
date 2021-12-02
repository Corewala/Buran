package corewala.buran.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ProcessTextActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val processText = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && intent.hasExtra(Intent.EXTRA_PROCESS_TEXT) -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString()
            else -> null
        }

        Intent(this, GemActivity::class.java).run {
            putExtra("process_text", processText)
            startActivity(this)
            finish()
        }
    }
}