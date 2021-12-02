package corewala

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.net.URI


fun View.visible(visible: Boolean) = when {
    visible -> this.visibility = View.VISIBLE
    else -> this.visibility = View.GONE
}

fun View.visibleRetainingSpace(visible: Boolean) = when {
    visible -> this.visibility = View.VISIBLE
    else -> this.visibility = View.INVISIBLE
}

fun View.hideKeyboard(){
    val imm: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard(){
    val imm: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun String.toURI(): URI {
    return URI.create(this)
}

fun URI.toUri(): Uri {
    return Uri.parse(this.toString())
}

fun Uri.toURI(): URI {
    return URI.create(this.toString())
}

fun Uri.isGemini(): Boolean{
    return this.toString().startsWith("gemini://")
}

@SuppressLint("DefaultLocale")
fun String.endsWithImage(): Boolean{
    return this.toLowerCase().endsWith(".png") ||
            this.toLowerCase().endsWith(".jpg") ||
            this.toLowerCase().endsWith(".jpeg") ||
            this.toLowerCase().endsWith(".gif")
}

@SuppressLint("DefaultLocale")
fun String.isWeb(): Boolean{
    return this.toLowerCase().startsWith("https://") ||
            this.toLowerCase().startsWith("http://")
}

fun delay(ms: Long, action: () -> Unit){
    object : CountDownTimer(ms, ms/2) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            action.invoke()
        }
    }.start()
}

fun Int.toPx(): Float {
    return (this.toFloat() * Resources.getSystem().displayMetrics.density)
}
