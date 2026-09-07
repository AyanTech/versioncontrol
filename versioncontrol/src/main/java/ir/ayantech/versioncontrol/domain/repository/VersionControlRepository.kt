package ir.ayantech.versioncontrol.domain.repository

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import kotlinx.coroutines.flow.Flow

interface VersionControlRepository {
    suspend fun checkVersion(config: VersionControlConfig): Result<UpdateStatus>
    suspend fun getLastVersion(config: VersionControlConfig): Result<UpdateInfo>
    fun downloadApk(url: String, destinationPath: String): Flow<DownloadState>
}
