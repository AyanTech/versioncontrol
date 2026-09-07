package ir.ayantech.versioncontrol.domain.model

sealed interface VersionCheckResult {
    data object UpToDate : VersionCheckResult
    data class UpdateAvailable(val updateInfo: UpdateInfo) : VersionCheckResult
    data class Failure(val message: String, val cause: Throwable? = null) : VersionCheckResult
}
