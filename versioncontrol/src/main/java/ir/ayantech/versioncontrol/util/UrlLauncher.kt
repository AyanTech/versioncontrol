package ir.ayantech.versioncontrol.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object UrlLauncher {

    fun openUrl(context: Context, url: String?) {
        if (url.isNullOrEmpty()) return
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                if (context !is Activity) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
