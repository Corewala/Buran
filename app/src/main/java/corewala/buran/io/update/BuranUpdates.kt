package corewala.buran.io.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import corewala.buran.BuildConfig
import corewala.buran.R
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class BuranUpdates {

    fun getLatestVersion(): String {
        var latestVersion = BuildConfig.VERSION_NAME

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

    fun installUpdate(context: Context, latestVersion: String){
        val updateUrl = "https://github.com/Corewala/Buran/releases/download/$latestVersion/Buran-$latestVersion.apk"
        var updateDestination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/Buran.apk"
        val fileUri = Uri.parse("file://$updateDestination")
        val packageFile = File(updateDestination)
        if(packageFile.exists()){
            packageFile.delete()
        }

        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(updateUrl)
        val request = DownloadManager.Request(downloadUri)

        request.setTitle(context.getString(R.string.app_name))
        request.setDescription("")
        request.setDestinationUri(fileUri)
        request.setMimeType("application/vnd.android.package-archive")

        val contentUri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            packageFile
        )

        val updateDownloadReceiver: BroadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context, intent: Intent){
                val intent = Intent(Intent.ACTION_VIEW)

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    intent.data = contentUri
                } else {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.setDataAndType(
                        fileUri,
                        "application/vnd.android.package-archive"
                    )
                }
                println("Installing update")
                context.startActivity(intent)
                context.unregisterReceiver(this)
            }
        }
        context.registerReceiver(updateDownloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        downloadManager.enqueue(request)
    }
}