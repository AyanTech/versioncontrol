package ir.ayantech.versioncontrol.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GetApplicationColocationConfigInputDto(
    @SerializedName("ApplicationType")
    val applicationType: String?,
    @SerializedName("ApplicationName")
    val applicationName: String?,
    @SerializedName("ColocationType")
    val colocationType: String?,
    @SerializedName("CurrentApplicationVersion")
    val currentApplicationVersion: String?
)
