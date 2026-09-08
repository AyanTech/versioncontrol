package ir.ayantech.versioncontrol.data.mapper

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCStatusDto
import ir.ayantech.versioncontrol.domain.model.ColocationEndpoint
import ir.ayantech.versioncontrol.domain.model.LinkType
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.model.UpdateStatus

fun VersionControlConfig.toCheckVersionInputDto(): CheckVersionInputDto {
    return CheckVersionInputDto(
        applicationName = applicationName,
        applicationType = applicationType,
        categoryName = categoryName,
        currentApplicationVersion = applicationVersion,
        extraInfo = extraInfo
    )
}

fun VersionControlConfig.toGetLastVersionInputDto(): GetLastVersionInputDto {
    return GetLastVersionInputDto(
        applicationName = applicationName,
        applicationType = applicationType,
        categoryName = categoryName,
        currentApplicationVersion = applicationVersion,
        extraInfo = extraInfo
    )
}

fun CheckVersionResponseDto?.toUIModel(): UpdateStatus {
    val rawStatus = this?.parameters?.updateStatus
    return UpdateStatus.fromRawValue(rawStatus)
}

fun GetLastVersionResponseDto?.toUIModel(): UpdateInfo {
    val params = this?.parameters
    return UpdateInfo(
        title = params?.title,
        body = params?.body,
        acceptButtonText = params?.acceptButtonText,
        rejectButtonText = params?.rejectButtonText,
        changeLogs = params?.changeLogs ?: emptyList(),
        linkType = LinkType.fromRawValue(params?.linkType),
        link = params?.link,
        textToShare = params?.textToShare
    )
}

fun String?.isResponseSuccessful(): Boolean {
    return this == "G00000" || this == "s00000"
}

fun VCStatusDto?.isResponseSuccessful(): Boolean {
    return this?.code.isResponseSuccessful()
}

fun VCStatusDto?.getExceptionOrNull(): IllegalStateException? {
    if (this.isResponseSuccessful()) return null
    val desc = this?.description?.takeIf { it.isNotBlank() }
    return IllegalStateException(desc ?: "Server error: ${this?.code ?: "unknown"}")
}

fun GetApplicationColocationConfigResponseDto?.toColocationEndpoints(): List<ColocationEndpoint> {
    return this?.parameters?.endpointList?.mapNotNull { dto ->
        val name = dto.name
        val url = dto.effectiveUrl
        if (!name.isNullOrBlank() && !url.isNullOrBlank()) {
            ColocationEndpoint(name = name, url = url)
        } else {
            null
        }
    } ?: emptyList()
}
