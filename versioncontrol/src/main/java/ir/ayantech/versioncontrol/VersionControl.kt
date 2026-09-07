package ir.ayantech.versioncontrol

import android.content.Context
import ir.ayantech.versioncontrol.data.downloader.HttpApkDownloader
import ir.ayantech.versioncontrol.data.remote.VersionControlRemoteDataSource
import ir.ayantech.versioncontrol.data.remote.VersionControlRemoteDataSourceImpl
import ir.ayantech.versioncontrol.data.repository.VersionControlRepositoryImpl
import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.model.VersionCheckResult
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.CheckVersionUseCase
import ir.ayantech.versioncontrol.domain.usecase.DownloadApkUseCase
import ir.ayantech.versioncontrol.domain.usecase.GetLastVersionUseCase
import ir.ayantech.versioncontrol.domain.usecase.ShareAppUseCase
import kotlinx.coroutines.flow.Flow

class VersionControl internal constructor(
    repository: VersionControlRepository
) {
    private val checkVersionUseCase = CheckVersionUseCase(repository)
    private val getLastVersionUseCase = GetLastVersionUseCase(repository)
    private val downloadApkUseCase = DownloadApkUseCase(repository)
    private val shareAppUseCase = ShareAppUseCase(repository)

    suspend fun checkForNewVersion(config: VersionControlConfig): VersionCheckResult {
        return checkVersionUseCase(config)
    }

    suspend fun getLastVersion(config: VersionControlConfig): Result<UpdateInfo> {
        return getLastVersionUseCase(config)
    }

    suspend fun shareApp(config: VersionControlConfig): Result<String> {
        return shareAppUseCase(config)
    }

    fun downloadApk(url: String, destinationPath: String): Flow<DownloadState> {
        return downloadApkUseCase(url, destinationPath)
    }

    companion object {
        @Volatile
        private var sharedRemoteDataSource: VersionControlRemoteDataSource? = null

        private fun getRemoteDataSource(): VersionControlRemoteDataSource {
            return sharedRemoteDataSource ?: synchronized(this) {
                sharedRemoteDataSource ?: VersionControlRemoteDataSourceImpl().also { sharedRemoteDataSource = it }
            }
        }

        fun create(context: Context): VersionControl {
            val downloader = HttpApkDownloader(context.applicationContext)
            val repository = VersionControlRepositoryImpl(
                remoteDataSource = getRemoteDataSource(),
                apkDownloader = downloader
            )
            return VersionControl(repository)
        }
    }
}
