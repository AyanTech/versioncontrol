package ir.ayantech.versioncontrol.data.remote.dto

import com.google.gson.annotations.SerializedName
import ir.ayantech.versioncontrol.model.ExtraInfoModel

data class CheckVersionInputDto(
    @SerializedName("ApplicationName")
    val applicationName: String?,
    @SerializedName("ApplicationType")
    val applicationType: String?,
    @SerializedName("CategoryName")
    val categoryName: String?,
    @SerializedName("CurrentApplicationVersion")
    val currentApplicationVersion: String?,
    @SerializedName("ExtraInfo")
    val extraInfo: ExtraInfoModel?
)
