package ir.ayantech.versioncontrol

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.widget.Toast
import androidx.core.app.ShareCompat
import ir.ayantech.versioncontrol.api.CheckVersion
import ir.ayantech.versioncontrol.api.GetLastVersion
import ir.ayantech.versioncontrol.api.VCResponseStatus
import ir.ayantech.versioncontrol.api.VersionControlAPI
import ir.ayantech.versioncontrol.api.VersionControlAPIs
import ir.ayantech.versioncontrol.model.ExtraInfoModel
import ir.ayantech.versioncontrol.model.VCResponseModel

class VersionControlCore private constructor() {

    private var applicationName: String? = null
    private var applicationType: String? = null
    private var applicationVersion: String? = null
    private var categoryName: String? = null
    private var extraInfo: ExtraInfoModel? = null
    private var typeface: Typeface? = null

    companion object {
        private var versionControlCoreInstance: VersionControlCore? = null
        private var baseUrl: String? = null

        @JvmStatic
        @Deprecated("Use getInstance(baseUrl) instead")
        fun getInstance(): VersionControlCore {
            if (versionControlCoreInstance == null) {
                versionControlCoreInstance = VersionControlCore()
            }
            return versionControlCoreInstance!!
        }

        @JvmStatic
        fun getInstance(baseUrl: String?): VersionControlCore {
            if (versionControlCoreInstance == null) {
                Companion.baseUrl = baseUrl
                versionControlCoreInstance = VersionControlCore()
            }
            return versionControlCoreInstance!!
        }

        @JvmStatic
        fun getApplicationVersion(context: Context): String {
            try {
                return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return ""
        }
    }

    init {
        VersionControlAPIs.initialize(baseUrl)
    }

    private fun initializeProperties(context: Context) {
        initializeApplicationType()
        initializeApplicationName(context)
        initializeApplicationVersion(context)
    }

    private fun initializeApplicationName(context: Context) {
        if (applicationName != null) return
        try {
            applicationName = context.packageName.split(".")[2]
        } catch (_: Exception) {
        }
    }

    private fun initializeApplicationVersion(context: Context) {
        if (applicationVersion == null) {
            setApplicationVersion(getApplicationVersion(context))
        }
    }

    private fun initializeApplicationType() {
        if (applicationType == null) {
            setApplicationType("android")
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

    fun checkForNewVersion(activity: Activity) {
        initializeProperties(activity)
        val checkVersionApi = VersionControlAPIs.checkVersion ?: return
        checkVersionApi.callApi(
            object : VCResponseStatus {
                override fun onSuccess(
                    versionControlAPI: VersionControlAPI<*, *>?,
                    message: String?,
                    responseModel: VCResponseModel?
                ) {
                    if (versionControlAPI is CheckVersion) {
                        val response = responseModel as? CheckVersion.CheckVersionResponse
                        if (response?.parameters?.updateStatus == CheckVersion.UpdateStatus.NOT_REQUIRED) {
                            return
                        }
                        VersionControlAPIs.getLastVersion?.callApi(
                            this,
                            GetLastVersion.GetLastVersionInputModel(
                                applicationName,
                                applicationType,
                                categoryName,
                                getApplicationVersion(activity),
                                extraInfo
                            )
                        )
                    } else if (versionControlAPI is GetLastVersion) {
                        val model = responseModel as? GetLastVersion.GetLastVersionResponseModel
                        val params = model?.parameters
                        VersionControlDialog(
                            activity,
                            params?.title,
                            params?.body,
                            params?.acceptButtonText,
                            params?.rejectButtonText,
                            params?.changeLogs,
                            params?.linkType,
                            params?.link,
                            VersionControlAPIs.checkVersion?.response?.parameters?.updateStatus,
                            typeface
                        ).show()
                    }
                }

                override fun onFail(
                    versionControlAPI: VersionControlAPI<*, *>?,
                    error: String?,
                    canTry: Boolean
                ) {
                }
            },
            CheckVersion.CheckVersionInputModel(
                applicationName,
                applicationType,
                categoryName,
                applicationVersion,
                extraInfo
            )
        )
    }

    fun shareApp(context: Context) {
        initializeProperties(context)
        val getLastVersionResponse = VersionControlAPIs.getLastVersion?.response
        if (getLastVersionResponse != null) {
            try {
                share(context, getLastVersionResponse.parameters?.textToShare)
            } catch (_: Exception) {
            }
        } else {
            VersionControlAPIs.getLastVersion?.callApi(
                object : VCResponseStatus {
                    override fun onSuccess(
                        versionControlAPI: VersionControlAPI<*, *>?,
                        message: String?,
                        responseModel: VCResponseModel?
                    ) {
                        val shareText = VersionControlAPIs.getLastVersion?.response?.parameters?.textToShare
                        share(context, shareText)
                    }

                    override fun onFail(
                        versionControlAPI: VersionControlAPI<*, *>?,
                        error: String?,
                        canTry: Boolean
                    ) {
                        Toast.makeText(
                            context,
                            "لطفا اتصال اینترنت خود را بررسی کرده و دوباره تلاش نمایید.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                GetLastVersion.GetLastVersionInputModel(
                    applicationName,
                    applicationType,
                    categoryName,
                    getApplicationVersion(context),
                    extraInfo
                )
            )
        }
    }

    private fun share(context: Context, shareBody: String?) {
        if (context is Activity) {
            ShareCompat.IntentBuilder(context)
                .setText(shareBody)
                .setType("text/plain")
                .setChooserTitle("به اشتراک گذاری از طریق:")
                .startChooser()
        }
    }
}
