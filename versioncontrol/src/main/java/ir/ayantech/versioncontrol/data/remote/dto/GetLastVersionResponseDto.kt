package ir.ayantech.versioncontrol.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GetLastVersionResponseDto(
    @SerializedName("Parameters")
    val parameters: Parameters?,
    @SerializedName("Status")
    val status: VCStatusDto?
) {
    data class Parameters(
        @SerializedName("ChangeLogs")
        val changeLogs: List<String>? = null,
        @SerializedName("Link")
        val link: String? = null,
        @SerializedName("LinkType")
        val linkType: String? = null,
        @SerializedName("TextToShare")
        val textToShare: String? = null,
        @SerializedName("Title")
        val title: String? = null,
        @SerializedName("Body")
        val body: String? = null,
        @SerializedName("AcceptButtonText")
        val acceptButtonText: String? = null,
        @SerializedName("RejectButtonText")
        val rejectButtonText: String? = null
    )
}
