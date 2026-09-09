package ir.ayantech.versioncontrol.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

object ApkInstaller {

    fun installApk(context: Context, filePath: String) {
        val apkFile = File(filePath)
        if (!apkFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }
        val apkUri = Uri.fromFile(apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    fun getDownloadDirectory(context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.filesDir.absolutePath
        } else if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(null)?.absolutePath
        } else {
            null
        }
    }
}
