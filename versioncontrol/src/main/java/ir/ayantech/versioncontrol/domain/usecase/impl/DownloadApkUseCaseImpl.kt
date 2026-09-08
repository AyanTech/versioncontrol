package ir.ayantech.versioncontrol.domain.usecase.impl

import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.DownloadApkUseCase
import kotlinx.coroutines.flow.Flow

class DownloadApkUseCaseImpl(
    private val repository: VersionControlRepository,
) : DownloadApkUseCase {

    override operator fun invoke(url: String, destinationPath: String): Flow<DownloadState> {
        return repository.downloadApk(url, destinationPath)
    }
}
