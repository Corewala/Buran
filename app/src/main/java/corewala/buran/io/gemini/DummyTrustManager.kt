package corewala.buran.io.gemini

import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException
import kotlin.jvm.Throws

object DummyTrustManager {

    fun get(): Array<TrustManager> {
        return arrayOf(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {

                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                    println("checkServerTrusted()")
                    println("checkServerTrusted() authType: $authType")
                    chain?.forEach { cert ->
                        println("checkServerTrusted() cert: ${cert.subjectDN}")
                    }
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })
    }
}