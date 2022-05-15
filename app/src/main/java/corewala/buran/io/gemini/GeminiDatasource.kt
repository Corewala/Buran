package corewala.buran.io.gemini

import android.content.Context
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import corewala.buran.Buran
import corewala.buran.OppenURI
import corewala.buran.io.GemState
import corewala.buran.io.database.history.BuranHistory
import corewala.buran.io.keymanager.BuranKeyManager
import corewala.toUri
import java.io.*
import java.lang.IllegalStateException
import java.net.ConnectException
import java.net.URI
import java.net.UnknownHostException
import javax.net.ssl.*


class GeminiDatasource(private val context: Context, val history: BuranHistory): Datasource {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val runtimeHistory = mutableListOf<URI>()
    private var forceDownload = false

    private var onUpdate: (state: GemState) -> Unit = {_ ->}

    private val buranKeyManager = BuranKeyManager(context){ keyError ->
        onUpdate(GemState.ClientCertError(GeminiResponse.Header(-3, keyError)))
    }

    private var socketFactory: SSLSocketFactory? = null

     override fun request(address: String, forceDownload: Boolean, clientCertAllowed: Boolean, onUpdate: (state: GemState) -> Unit) {
        this.forceDownload = forceDownload

        this.onUpdate = onUpdate

        val uri = URI.create(address)

        onUpdate(GemState.Requesting(uri))

        GlobalScope.launch {
            geminiRequest(uri, onUpdate, clientCertAllowed)
        }
    }

    private fun initSSLFactory(protocol: String, clientCertAllowed: Boolean){
        val sslContext = when (protocol) {
            "TLS_ALL" -> SSLContext.getInstance("TLS")
            else -> SSLContext.getInstance(protocol)
        }

        sslContext.init(buranKeyManager.getFactory(clientCertAllowed)?.keyManagers, DummyTrustManager.get(), null)
        socketFactory = sslContext.socketFactory
    }

    private fun geminiRequest(uri: URI, onUpdate: (state: GemState) -> Unit, clientCertAllowed: Boolean){
        val protocol = "TLS"
        val useClientCert = prefs.getBoolean(Buran.PREF_KEY_CLIENT_CERT_ACTIVE, false)

        //Update factory if operating mode has changed
        when {
            socketFactory == null -> initSSLFactory(protocol!!, clientCertAllowed)
            useClientCert && !buranKeyManager.lastCallUsedKey -> initSSLFactory(protocol!!, clientCertAllowed)
            !useClientCert && buranKeyManager.lastCallUsedKey -> initSSLFactory(protocol!!, clientCertAllowed)
        }

        val socket: SSLSocket?
        try {
            socket = socketFactory?.createSocket(uri.host, 1965) as SSLSocket

            println("Buran socket handshake with ${uri.host}")
            socket.startHandshake()
        }catch (uhe: UnknownHostException){
            println("Buran socket error, unknown host: $uhe")
            onUpdate(GemState.ResponseUnknownHost(uri))
            return
        }catch (ce: ConnectException){
            println("Buran socket error, connect exception: $ce")
            onUpdate(GemState.ResponseError(GeminiResponse.Header(-1, ce.message ?: ce.toString())))
            return
        }catch (she: SSLHandshakeException){
            println("Buran socket error, ssl handshake exception: $she")
            onUpdate(GemState.ResponseError(GeminiResponse.Header(-2, she.message ?: she.toString())))
            return
        }

        // OUT >>>>>>>>>>>>>>>>>>>>>>>>>>
        val outputStreamWriter = OutputStreamWriter(socket.outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        val outWriter = PrintWriter(bufferedWriter)

        val requestEntity = uri.toString() + "\r\n"
        println("Buran socket requesting $requestEntity")
        outWriter.print(requestEntity)
        outWriter.flush()

        if (outWriter.checkError()) {
            onUpdate(GemState.ResponseError(GeminiResponse.Header(-1, "Print Writer Error")))
            outWriter.close()
            return
        }

        // IN <<<<<<<<<<<<<<<<<<<<<<<<<<<

        val inputStream = socket.inputStream
        val headerInputReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(headerInputReader)
        val headerLine = bufferedReader.readLine()

        println("Buran: response header: $headerLine")

        if(headerLine == null){
            onUpdate(GemState.ResponseError(GeminiResponse.Header(-2, "Server did not respond with a Gemini header: $uri")))
            return
        }

        val header = GeminiResponse.parseHeader(headerLine)

        when {
            header.code == GeminiResponse.INPUT -> onUpdate(GemState.ResponseInput(uri, header))
            header.code == GeminiResponse.REDIRECT -> request(resolve(uri.host, header.meta), false, false, onUpdate)
            header.code == GeminiResponse.CLIENT_CERTIFICATE_REQUIRED -> onUpdate(GemState.ClientCertRequired(uri, header))
            header.code != GeminiResponse.SUCCESS -> onUpdate(GemState.ResponseError(header))
            header.meta.startsWith("text/gemini") -> getGemtext(bufferedReader, uri, header, onUpdate)
            header.meta.startsWith("text/") -> getString(socket, uri, header, onUpdate)
            header.meta.startsWith("image/") -> getBinary(socket, uri, header, onUpdate)
            else -> {
                //File served over Gemini but not handled in-app, eg .pdf
                if(forceDownload){
                    getBinary(socket, uri, header, onUpdate)
                }else{
                    onUpdate(GemState.ResponseUnknownMime(uri, header))
                }
            }
        }

        //Close input
        bufferedReader.close()
        headerInputReader.close()

        //Close output:
        outputStreamWriter.close()
        bufferedWriter.close()
        outWriter.close()

        socket.close()
    }

    private fun getGemtext(reader: BufferedReader, uri: URI, header: GeminiResponse.Header, onUpdate: (state: GemState) -> Unit){

        val lines = mutableListOf<String>()

        lines.addAll(reader.readLines())

        val processed = GemtextHelper.findCodeBlocks(lines)

        when {
            !uri.toString().startsWith("gemini://") -> throw IllegalStateException("Not a Gemini Uri")
        }

        updateHistory(uri)
        onUpdate(GemState.ResponseGemtext(uri, header, processed))
    }

    private fun updateHistory(uri: URI) {
        if (runtimeHistory.isEmpty() || runtimeHistory.last().toString() != uri.toString()) {
            runtimeHistory.add(uri)
            println("Buran added $uri to runtime history (size ${runtimeHistory.size})")
        }

        history.add(uri.toUri()){}
    }

    private fun getString(socket: SSLSocket?, uri: URI, header: GeminiResponse.Header, onUpdate: (state: GemState) -> Unit){
        val content = socket?.inputStream?.bufferedReader().use {
            reader -> reader?.readText()
        }
        socket?.close()
        onUpdate(GemState.ResponseText(uri, header, content ?: "Error fetching content"))
    }

    private fun getBinary(socket: SSLSocket?, uri: URI, header: GeminiResponse.Header, onUpdate: (state: GemState) -> Unit){

        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        val filename = (1..12)
            .map{charset.random()}
            .joinToString("")

        val host = uri.host.replace(".", "_")
        val cacheName = "${host}_$filename"
        println("Caching file: $filename from uri: $uri, cacheName: $cacheName")

        val cacheFile = File(context.cacheDir, cacheName)

        when {
            cacheFile.exists() -> {
                when {
                    header.meta.startsWith("image/") -> onUpdate(GemState.ResponseImage(uri, header, cacheFile.toUri()))
                    else -> onUpdate(GemState.ResponseBinary(uri, header, cacheFile.toUri()))
                }
            }
            else -> {
                cacheFile.createNewFile()
                cacheFile.outputStream().use{ outputStream ->
                    socket?.inputStream?.copyTo(outputStream)
                    socket?.close()
                }

                when {
                    header.meta.startsWith("image/") -> onUpdate(GemState.ResponseImage(uri, header, cacheFile.toUri()))
                    else -> onUpdate(GemState.ResponseBinary(uri, header, cacheFile.toUri()))
                }
            }
        }
    }

    private fun resolve(host: String, address: String): String{
        val ouri = OppenURI()
        ouri.set(host)
        return ouri.resolve(address)
    }

    override fun canGoBack(): Boolean = runtimeHistory.isEmpty() || runtimeHistory.size > 1

    override fun goBack(onUpdate: (state: GemState) -> Unit) {
        runtimeHistory.removeLast()
        request(runtimeHistory.last().toString(), false, false, onUpdate)
    }

    //This just forces the factory to rebuild before the next request
    fun invalidate() {
        socketFactory = null
    }
}