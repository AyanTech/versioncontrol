package ir.ayantech.versioncontrol.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EndpointDto(
    @SerializedName("Name")
    val name: String? = null,
    @SerializedName("Url")
    val url: String? = null,
    @SerializedName("URL")
    val upperUrl: String? = null
) {
    val effectiveUrl: String?
        get() = url?.takeIf { it.isNotBlank() } ?: upperUrl?.takeIf { it.isNotBlank() }
}

data class GetApplicationColocationConfigParametersDto(
    @SerializedName("EndpointList")
    val endpointList: List<EndpointDto>? = null
)

data class GetApplicationColocationConfigResponseDto(
    @SerializedName("Parameters")
    val parameters: GetApplicationColocationConfigParametersDto? = null,
    @SerializedName("Status")
    val status: VCStatusDto? = null
)
