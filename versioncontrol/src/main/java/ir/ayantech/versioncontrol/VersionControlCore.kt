package ir.ayantech.versioncontrol

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.widget.Toast
import androidx.core.app.ShareCompat
import ir.ayantech.versioncontrol.domain.model.VersionCheckResult
import ir.ayantech.versioncontrol.model.ExtraInfoModel
import ir.ayantech.versioncontrol.ui.VersionControlDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VersionControlCore private constructor(
    private var baseUrl: String
) {
    private var applicationName: String? = null
    private var applicationType: String? = null
    private var applicationVersion: String? = null
    private var categoryName: String? = null
    private var extraInfo: ExtraInfoModel? = null
    private var typeface: Typeface? = null

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
        @Volatile
        private var instance: VersionControlCore? = null

        @JvmStatic
        fun getInstance(baseUrl: String): VersionControlCore {
            require(baseUrl.isNotBlank()) { "Base URL must be provided by the application." }
            return (instance ?: synchronized(this) {
                instance ?: VersionControlCore(baseUrl).also { instance = it }
            }).apply {
                this.baseUrl = baseUrl
            }
        }

        @JvmStatic
        fun getApplicationVersion(context: Context): String {
            return try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
            } catch (_: PackageManager.NameNotFoundException) {
                ""
            }
        }
    }

    private fun initializeProperties(context: Context) {
        if (applicationType == null) {
            applicationType = VersionControlConfig.DEFAULT_APPLICATION_TYPE
        }
        if (applicationName == null) {
            try {
                applicationName = context.packageName.split(".")[2]
            } catch (_: Exception) {
            }
        }
        if (applicationVersion == null) {
            applicationVersion = getApplicationVersion(context)
        }
    }

    fun setApplicationName(applicationName: String?): VersionControlCore {
        this.applicationName = applicationName
        return this
    }

    fun setApplicationType(applicationType: String?): VersionControlCore {
        this.applicationType = applicationType
        return this
    }

    fun setCategoryName(categoryName: String?): VersionControlCore {
        this.categoryName = categoryName
        return this
    }

    fun setApplicationVersion(applicationVersion: String?): VersionControlCore {
        this.applicationVersion = applicationVersion
        return this
    }

    fun setExtraInfo(extraInfo: ExtraInfoModel?): VersionControlCore {
        this.extraInfo = extraInfo
        return this
    }

    fun setTypeface(typeface: Typeface?): VersionControlCore {
        this.typeface = typeface
        return this
    }

    private fun buildConfig(): VersionControlConfig {
        require(baseUrl.isNotBlank()) { "Base URL must be provided by the application." }
        return VersionControlConfig(
            baseUrl = baseUrl,
            applicationName = applicationName,
            applicationType = applicationType ?: VersionControlConfig.DEFAULT_APPLICATION_TYPE,
            categoryName = categoryName,
            applicationVersion = applicationVersion,
            extraInfo = extraInfo,
            typeface = typeface
        )
    }

    fun checkForNewVersion(activity: Activity) {
        initializeProperties(activity)
        val config = buildConfig()
        val versionControl = VersionControl.create(activity)

        mainScope.launch {
            val result = versionControl.checkForNewVersion(config)
            if (result is VersionCheckResult.UpdateAvailable) {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    VersionControlDialog(
                        activity = activity,
                        updateInfo = result.updateInfo,
                        typeface = typeface,
                        versionControl = versionControl
                    ).show()
                }
            }
        }
    }

    fun shareApp(context: Context) {
        initializeProperties(context)
        val config = buildConfig()
        val versionControl = VersionControl.create(context)

        mainScope.launch {
            val shareResult = versionControl.shareApp(config)
            shareResult.fold(
                onSuccess = { shareText ->
                    share(context, shareText)
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.vc_check_internet_connection),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
    }

    private fun share(context: Context, shareBody: String?) {
        if (shareBody.isNullOrEmpty()) return
        if (context is Activity) {
            ShareCompat.IntentBuilder(context)
                .setText(shareBody)
                .setType("text/plain")
                .setChooserTitle(context.getString(R.string.vc_share_via))
                .startChooser()
        }
    }
}
