package corewala.buran.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import corewala.buran.io.gemini.Datasource
import corewala.buran.io.GemState
import corewala.buran.io.database.BuranDatabase
import corewala.buran.io.gemini.GeminiDatasource
import java.net.URI

class GemViewModel: ViewModel() {

    private lateinit var gemini: Datasource
    private lateinit var db: BuranDatabase
    private var onState: (state: GemState) -> Unit = {}

    fun initialise(home: String, gemini: Datasource, db: BuranDatabase, onState: (state: GemState) -> Unit){
        this.gemini = gemini
        this.db = db
        this.onState = onState

        request(home, false)
    }

    fun request(address: String, clientCertAllowed: Boolean) {
        gemini.request(address, false, clientCertAllowed){ state ->
            onState(state)
        }
    }

    fun requestBinaryDownload(uri: URI) {
        gemini.request(uri.toString(), true, false){ state ->
            onState(state)
        }
    }

    //todo - same action as above... refactor
    fun requestInlineImage(uri: URI, onImageReady: (cacheUri: Uri?) -> Unit){
        gemini.request(uri.toString(), false, false){ state ->
            when (state) {
                is GemState.ResponseImage -> onImageReady(state.cacheUri)
                else -> onState(state)
            }
        }
    }

    //If user changes client cert prefs in Settings this awful hack causes it to refresh state on next request
    fun invalidateDatasource() {
        if(gemini is GeminiDatasource){
            (gemini as GeminiDatasource).invalidate()
        }
    }
}