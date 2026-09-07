package ir.ayantech.versioncontrol

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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

class VersionControlDialog(
    private val activity: Activity,
    title: String?,
    message: String?,
    positiveButton: String?,
    negativeButton: String?,
    changeLogs: ArrayList<String>?,
    linkType: String?,
    link: String?,
    updateStatus: String?,
    typeface: Typeface?
) : Dialog(activity) {

    private var id = -1
    private var manager: DownloadManager

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_version_control)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        findViewById<TextView>(R.id.titleTv).text = title
        findViewById<TextView>(R.id.messageTv).text = message
        findViewById<TextView>(R.id.positiveTv).text = positiveButton
        findViewById<TextView>(R.id.negativeTv).text = negativeButton

        if (changeLogs.isNullOrEmpty()) {
            findViewById<View>(R.id.changeLogTv).visibility = View.GONE
        } else {
            val changeLog = StringBuilder()
            for (s in changeLogs) {
                changeLog.append(s).append("\n")
            }
            findViewById<TextView>(R.id.changeLogTv).text = changeLog
        }

        manager = DownloadManager.Builder()
            .context(context)
            .downloader(OkHttpDownloader.create())
            .threadPoolSize(2)
            .build()

        findViewById<View>(R.id.positiveTv).setOnClickListener {
            try {
                if (GetLastVersion.LinkType.DIRECT == linkType) {
                    val rootDirPath = getRootDirPath(context)
                    if (rootDirPath == null) {
                        openUrl(context, link)
                        return@setOnClickListener
                    }
                    findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
                    findViewById<View>(R.id.progressTv).visibility = View.VISIBLE
                    val destPath = "$rootDirPath/newversion${Date().time}.apk"
                    val request = DownloadRequest.Builder()
                        .url(link)
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
                                    installApp(context, filePath)
                                    dismiss()
                                    if (CheckVersion.UpdateStatus.MANDATORY == updateStatus) {
                                        activity.finish()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String?) {
                                dismiss()
                                openUrl(context, link)
                            }
                        })
                        .build()

                    id = manager.add(request)
                } else if (GetLastVersion.LinkType.PAGE == linkType) {
                    dismiss()
                    openUrl(context, link)
                    if (CheckVersion.UpdateStatus.MANDATORY == updateStatus) {
                        activity.finish()
                    }
                }
            } catch (_: Exception) {
                dismiss()
                openUrl(context, link)
            }
        }

        findViewById<View>(R.id.negativeTv).setOnClickListener {
            try {
                manager.cancel(id)
            } catch (_: Exception) {
            }
            dismiss()
            if (CheckVersion.UpdateStatus.MANDATORY == updateStatus) {
                activity.finish()
            }
        }

        if (typeface != null) {
            val mainContentLl = findViewById<LinearLayout>(R.id.mainContentLl)
            if (mainContentLl != null) {
                for (i in 0 until mainContentLl.childCount) {
                    val child = mainContentLl.getChildAt(i)
                    if (child is TextView) {
                        child.typeface = typeface
                    }
                }
            }
            findViewById<TextView>(R.id.positiveTv).typeface = typeface
            findViewById<TextView>(R.id.negativeTv).typeface = typeface
        }
    }

    @Deprecated("Deprecated in Java / Android")
    override fun onBackPressed() {
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
