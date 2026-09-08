package ir.ayantech.versioncontrol.domain.model

data class ColocationConfigResult(
    val endpointList: List<ColocationEndpoint>,
    val versionControlBaseUrl: String?,
)
