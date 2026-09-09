package ir.ayantech.versioncontrol.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VCRequestDto<T>(
    @SerializedName("Parameters")
    val parameters: T?
)
