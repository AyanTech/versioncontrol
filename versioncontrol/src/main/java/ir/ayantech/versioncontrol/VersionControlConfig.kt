package ir.ayantech.versioncontrol

import android.graphics.Typeface
import ir.ayantech.versioncontrol.model.ExtraInfoModel

data class VersionControlConfig(
    val baseUrl: String,
    val applicationName: String? = null,
    val applicationType: String = DEFAULT_APPLICATION_TYPE,
    val categoryName: String? = null,
    val applicationVersion: String? = null,
    val extraInfo: ExtraInfoModel? = null,
    val typeface: Typeface? = null,
    val iranBaseUrl: String? = null,
    val internationalBaseUrl: String? = null
) {
    companion object {
        const val DEFAULT_APPLICATION_TYPE = "android"
    }
}
