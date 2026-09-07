package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import kotlinx.coroutines.flow.Flow

class DownloadApkUseCase(
    private val repository: VersionControlRepository
) {
    operator fun invoke(url: String, destinationPath: String): Flow<DownloadState> {
        return repository.downloadApk(url, destinationPath)
    }
}
