package ir.ayantech.versioncontrol.domain.model

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progressPercent: Int) : DownloadState
    data class Success(val filePath: String) : DownloadState
    data class Error(val message: String?, val cause: Throwable? = null) : DownloadState
}
