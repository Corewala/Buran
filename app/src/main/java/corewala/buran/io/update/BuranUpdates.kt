package corewala.buran.io.update

import java.net.URL
import java.net.HttpURLConnection


class BuranUpdates {

    fun getLatestVersion(): String {
        var latestVersion = ""

        val updateCheckThread = Thread {
            val url = "https://github.com/Corewala/Buran/releases/latest"

            val con = URL(url).openConnection() as HttpURLConnection
            con.connect()
            con.getInputStream()

            latestVersion = con.getURL().toString().drop(47)
        }

        updateCheckThread.start()
        updateCheckThread.join()
        println("Latest version: $latestVersion")

        return latestVersion
    }
}