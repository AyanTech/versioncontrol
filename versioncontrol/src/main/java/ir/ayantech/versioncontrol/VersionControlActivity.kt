package ir.ayantech.versioncontrol

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.coolerfall.download.DownloadCallback
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.DownloadRequest
import com.coolerfall.download.OkHttpDownloader
import com.coolerfall.download.Priority
import ir.ayantech.versioncontrol.api.CheckVersion
import ir.ayantech.versioncontrol.api.GetLastVersion
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit

class VersionControlActivity : AppCompatActivity() {

    private var id = -1
    private var manager: DownloadManager? = null

    @Deprecated("Deprecated in Java / Android")
    override fun onBackPressed() {
    }

    private fun getVCTitle(): String? = intent.getStringExtra("title")
    private fun getVCMessage(): String? = intent.getStringExtra("message")
    private fun getVCPositiveButton(): String? = intent.getStringExtra("pos_btn")
    private fun getVCNegativeButton(): String? = intent.getStringExtra("neg_btn")
    private fun getVCLinkType(): String? = intent.getStringExtra("link_type")
    private fun getVCLink(): String? = intent.getStringExtra("link")
    private fun getVCUpdateStatus(): String? = intent.getStringExtra("update_status")
    private fun getVCChangeLogs(): ArrayList<String>? = intent.getStringArrayListExtra("change_logs")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_version_control)
        setFinishOnTouchOutside(false)

        findViewById<TextView>(R.id.titleTv).text = getVCTitle()
        findViewById<TextView>(R.id.messageTv).text = getVCMessage()
        findViewById<TextView>(R.id.positiveTv).text = getVCPositiveButton()
        findViewById<TextView>(R.id.negativeTv).text = getVCNegativeButton()

        val logs = getVCChangeLogs()
        if (logs.isNullOrEmpty()) {
            findViewById<View>(R.id.changeLogTv).visibility = View.GONE
        } else {
            val changeLog = StringBuilder()
            for (s in logs) {
                changeLog.append(s).append("\n")
            }
            findViewById<TextView>(R.id.changeLogTv).text = changeLog
        }

        manager = DownloadManager.Builder()
            .context(this)
            .downloader(OkHttpDownloader.create())
            .threadPoolSize(2)
            .build()

        findViewById<View>(R.id.positiveTv).setOnClickListener {
            try {
                if (GetLastVersion.LinkType.DIRECT == getVCLinkType()) {
                    val rootDirPath = getRootDirPath(this@VersionControlActivity)
                    if (rootDirPath == null) {
                        openUrl(this@VersionControlActivity, getVCLink())
                        return@setOnClickListener
                    }
                    findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
                    findViewById<View>(R.id.progressTv).visibility = View.VISIBLE
                    val destPath = "$rootDirPath/newversion${Date().time}.apk"
                    val request = DownloadRequest.Builder()
                        .url(getVCLink())
                        .retryTime(5)
                        .retryInterval(2, TimeUnit.SECONDS)
                        .progressInterval(100, TimeUnit.MILLISECONDS)
                        .priority(Priority.HIGH)
                        .destinationFilePath(destPath)
                        .downloadCallback(object : DownloadCallback() {
                            override fun onStart(downloadId: Int, totalBytes: Long) {}
                            override fun onRetry(downloadId: Int) {}

                            override fun onProgress(downloadId: Int, bytesWritten: Long, totalBytes: Long) {
                                val progressPercent = (bytesWritten * 100) / totalBytes
                                findViewById<ProgressBar>(R.id.progressBar).progress = progressPercent.toInt()
                                findViewById<TextView>(R.id.progressTv).text = String.format("%%%s", progressPercent)
                            }

                            override fun onSuccess(downloadId: Int, filePath: String) {
                                try {
                                    installApp(this@VersionControlActivity, filePath)
                                    if (CheckVersion.UpdateStatus.MANDATORY == getVCUpdateStatus()) {
                                        finish()
                                        endApplication()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String?) {
                                finish()
                                openUrl(this@VersionControlActivity, getVCLink())
                            }
                        })
                        .build()

                    id = manager?.add(request) ?: -1
                } else if (GetLastVersion.LinkType.PAGE == getVCLinkType()) {
                    finish()
                    openUrl(this@VersionControlActivity, getVCLink())
                    if (CheckVersion.UpdateStatus.MANDATORY == getVCUpdateStatus()) {
                        endApplication()
                    }
                }
            } catch (_: Exception) {
                finish()
                openUrl(this@VersionControlActivity, getVCLink())
            }
        }

        findViewById<View>(R.id.negativeTv).setOnClickListener {
            try {
                manager?.cancel(id)
            } catch (_: Exception) {
            }
            finish()
            if (CheckVersion.UpdateStatus.MANDATORY == getVCUpdateStatus()) {
                endApplication()
            }
        }
    }

    private fun endApplication() {
        Process.sendSignal(Process.myPid(), Process.SIGNAL_KILL)
    }

    private fun installApp(context: Context, path: String) {
        val toInstall = File(path)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", toInstall)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val apkUri = Uri.fromFile(toInstall)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        }
    }

    private fun openUrl(context: Context, url: String?) {
        if (url.isNullOrEmpty()) return
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(browserIntent)
    }

    private fun getRootDirPath(context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.filesDir.absolutePath
        } else if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(null)?.absolutePath
        } else {
            null
        }
    }
}
