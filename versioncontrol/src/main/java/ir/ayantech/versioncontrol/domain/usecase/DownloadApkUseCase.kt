package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface DownloadApkUseCase {
    operator fun invoke(url: String, destinationPath: String): Flow<DownloadState>
}
