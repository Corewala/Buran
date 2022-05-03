package corewala.buran.io

import android.net.Uri
import corewala.buran.io.gemini.GeminiResponse
import java.net.URI

sealed class GemState {
    data class AppQuery(val uri: URI): GemState()
    data class Requesting(val uri: URI): GemState()
    data class NotGeminiRequest(val uri: URI) : GemState()
    data class ResponseGemtext(val uri: URI, val header: GeminiResponse.Header, val lines: List<String>) : GemState()
    data class ResponseInput(val uri: URI, val header: GeminiResponse.Header) : GemState()
    data class ResponseText(val uri: URI, val header: GeminiResponse.Header, val content: String) : GemState()
    data class ResponseImage(val uri: URI, val header: GeminiResponse.Header, val cacheUri: Uri) : GemState()
    data class ResponseBinary(val uri: URI, val header: GeminiResponse.Header, val cacheUri: Uri) : GemState()
    data class ResponseUnknownMime(val uri: URI, val header: GeminiResponse.Header) : GemState()
    data class ResponseError(val header: GeminiResponse.Header): GemState()
    data class ResponseUnknownHost(val uri: URI): GemState()
    data class ClientCertRequired(val uri: URI, val header: GeminiResponse.Header): GemState()
    data class ClientCertError(val header: GeminiResponse.Header): GemState()
    object Blank: GemState()
}