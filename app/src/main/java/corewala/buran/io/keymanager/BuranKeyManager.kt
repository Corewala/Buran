package corewala.buran.io.keymanager

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import corewala.buran.Buran
import corewala.buran.R
import java.io.FileNotFoundException
import java.io.IOException
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory


class BuranKeyManager(val context: Context, val onKeyError: (error: String) -> Unit) {

    var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var lastCallUsedKey = false

    //If the user has a key loaded load it here - or else return null
    fun getFactory(clientCertAllowed: Boolean): KeyManagerFactory? {
        val isClientCertActive = prefs.getBoolean(Buran.PREF_KEY_CLIENT_CERT_ACTIVE, false)
        return when {
            isClientCertActive and clientCertAllowed -> {
                lastCallUsedKey = true
                val keyStore: KeyStore = KeyStore.getInstance("pkcs12")

                val uriStr = prefs.getString(Buran.PREF_KEY_CLIENT_CERT_URI, "")
                val password = prefs.getString(Buran.PREF_KEY_CLIENT_CERT_PASSWORD, "")
                val uri = Uri.parse(uriStr)
                try {
                    context.contentResolver?.openInputStream(uri)?.use {
                        try {
                            keyStore.load(it, password?.toCharArray())
                            val keyManagerFactory: KeyManagerFactory =
                                KeyManagerFactory.getInstance("X509")
                            keyManagerFactory.init(keyStore, password?.toCharArray())
                            return@use keyManagerFactory
                        } catch (ioe: IOException) {
                            onKeyError("${ioe.message}")
                            return null
                        }
                    }
                } catch(fnf: FileNotFoundException){
                    onKeyError("Please link your client certificate again in Settings; after an update Buran loses permissions to access external files, or the certificate has been moved/deleted\n\n${fnf.message}")
                    return null
                }
            }
            else -> {
                lastCallUsedKey = false
                null
            }
        }
    }

    //Working example with cert packaged with app
    fun getFactoryDemo(context: Context): KeyManagerFactory? {
        val keyStore: KeyStore = KeyStore.getInstance("pkcs12")
        keyStore.load(context.resources.openRawResource(R.raw.cert), "PASSWORD".toCharArray())

        val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("X509")
        keyManagerFactory.init(keyStore, "PASSWORD".toCharArray())

        return keyManagerFactory
    }
}