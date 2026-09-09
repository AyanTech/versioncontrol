package ir.ayantech.versioncontrol.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CheckVersionResponseDto(
    @SerializedName("Parameters")
    val parameters: Parameters?,
    @SerializedName("Status")
    val status: VCStatusDto?
) {
    data class Parameters(
        @SerializedName("UpdateStatus")
        val updateStatus: String?
    )
}
