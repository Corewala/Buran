package corewala.buran.io.gemini

import android.content.Context
import corewala.buran.io.GemState
import corewala.buran.io.database.history.BuranHistory
import java.net.URI

interface Datasource {
    fun request(address: String, forceDownload: Boolean, clientCertPassword: String?, onUpdate: (state: GemState) -> Unit)
    fun isRequesting(): Boolean
    fun cancel()
    fun canGoBack(): Boolean
    companion object{
        fun factory(context: Context, history: BuranHistory): Datasource {
            return GeminiDatasource(context, history)
        }
    }
}