package ir.ayantech.versioncontrol.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VCStatusDto(
    @SerializedName("Code")
    val code: String? = null,
    @SerializedName("Description")
    val description: String? = null
)
