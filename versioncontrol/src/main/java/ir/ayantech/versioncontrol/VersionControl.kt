package ir.ayantech.versioncontrol

import android.content.Context
import ir.ayantech.versioncontrol.data.downloader.HttpApkDownloader
import ir.ayantech.versioncontrol.data.remote.VersionControlRemoteDataSource
import ir.ayantech.versioncontrol.data.remote.VersionControlRemoteDataSourceImpl
import ir.ayantech.versioncontrol.data.repository.VersionControlRepositoryImpl
import ir.ayantech.versioncontrol.domain.model.ColocationConfigResult
import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.model.VersionCheckResult
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.CheckVersionUseCase
import ir.ayantech.versioncontrol.domain.usecase.impl.CheckVersionUseCaseImpl
import ir.ayantech.versioncontrol.domain.usecase.DownloadApkUseCase
import ir.ayantech.versioncontrol.domain.usecase.impl.DownloadApkUseCaseImpl
import ir.ayantech.versioncontrol.domain.usecase.GetApplicationColocationConfigUseCase
import ir.ayantech.versioncontrol.domain.usecase.impl.GetApplicationColocationConfigUseCaseImpl
import ir.ayantech.versioncontrol.domain.usecase.GetLastVersionUseCase
import ir.ayantech.versioncontrol.domain.usecase.impl.GetLastVersionUseCaseImpl
import ir.ayantech.versioncontrol.domain.usecase.ShareAppUseCase
import ir.ayantech.versioncontrol.domain.usecase.impl.ShareAppUseCaseImpl
import kotlinx.coroutines.flow.Flow

class VersionControl internal constructor(
    repository: VersionControlRepository,
) {
    private val checkVersionUseCase: CheckVersionUseCase = CheckVersionUseCaseImpl(repository)
    private val getLastVersionUseCase: GetLastVersionUseCase = GetLastVersionUseCaseImpl(repository)
    private val downloadApkUseCase: DownloadApkUseCase = DownloadApkUseCaseImpl(repository)
    private val shareAppUseCase: ShareAppUseCase = ShareAppUseCaseImpl(repository)
    private val getApplicationColocationConfigUseCase: GetApplicationColocationConfigUseCase =
        GetApplicationColocationConfigUseCaseImpl(repository)

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

    suspend fun getApplicationColocationConfig(
        applicationName: String,
        applicationType: String = VersionControlConfig.DEFAULT_APPLICATION_TYPE,
        applicationVersion: String,
        iranBaseUrl: String,
        internationalBaseUrl: String
    ): Result<ColocationConfigResult> {
        return getApplicationColocationConfigUseCase(
            applicationName = applicationName,
            applicationType = applicationType,
            applicationVersion = applicationVersion,
            iranBaseUrl = iranBaseUrl,
            internationalBaseUrl = internationalBaseUrl
        )
    }

    suspend fun getApplicationColocationConfig(
        config: VersionControlConfig
    ): Result<ColocationConfigResult> {
        val iranBaseUrl = config.iranBaseUrl
            ?.takeIf { it.isNotBlank() }
            ?: return Result.failure(
                IllegalArgumentException("Iran base URL must be provided by the application.")
            )
        val internationalBaseUrl = config.internationalBaseUrl
            ?.takeIf { it.isNotBlank() }
            ?: return Result.failure(
                IllegalArgumentException("International base URL must be provided by the application.")
            )
        return getApplicationColocationConfig(
            applicationName = config.applicationName ?: "",
            applicationType = config.applicationType,
            applicationVersion = config.applicationVersion ?: "",
            iranBaseUrl = iranBaseUrl,
            internationalBaseUrl = internationalBaseUrl
        )
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
