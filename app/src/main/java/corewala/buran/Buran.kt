package corewala.buran

import android.app.Application

class Buran: Application() {

    companion object{
        const val DEFAULT_HOME_CAPSULE = "gemini://tlgs.one"
        const val DEFAULT_SEARCH_BASE = "gemini://tlgs.one/search?"

        const val PREF_KEY_CLIENT_CERT_URI = "client_cert_uri"
        const val PREF_KEY_CLIENT_CERT_HUMAN_READABLE = "client_cert_uri_human_readable"
        const val PREF_KEY_CLIENT_CERT_PASSWORD = "client_cert_password"
        const val CLIENT_CERT_PASSWORD_SECRET_KEY_NAME = "client_cert_secret_key_name"
        const val PREF_KEY_USE_CUSTOM_TAB = "use_custom_tabs"
    }
}