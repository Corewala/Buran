package corewala.buran

import android.app.Application

class Buran: Application() {

    companion object{
        const val DEFAULT_HOME_CAPSULE = "gemini://rawtext.club/~sloum/spacewalk.gmi"

        const val FEATURE_CLIENT_CERTS = true

        const val PREF_KEY_CLIENT_CERT_URI = "client_cert_uri"
        const val PREF_KEY_CLIENT_CERT_HUMAN_READABLE = "client_cert_uri_human_readable"
        const val PREF_KEY_CLIENT_CERT_ACTIVE = "client_cert_active"
        const val PREF_KEY_CLIENT_CERT_PASSWORD = "client_cert_password"
        const val PREF_KEY_USE_CUSTOM_TAB = "use_custom_tabs"
    }
}