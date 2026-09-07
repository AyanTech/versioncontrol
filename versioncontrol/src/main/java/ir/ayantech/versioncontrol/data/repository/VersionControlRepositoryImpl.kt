package ir.ayantech.versioncontrol.data.repository

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.downloader.ApkDownloader
import ir.ayantech.versioncontrol.data.mapper.isResponseSuccessful
import ir.ayantech.versioncontrol.data.mapper.toCheckVersionInputDto
import ir.ayantech.versioncontrol.data.mapper.toGetLastVersionInputDto
import ir.ayantech.versioncontrol.data.mapper.toUIModel
import ir.ayantech.versioncontrol.data.remote.VersionControlRemoteDataSource
import ir.ayantech.versioncontrol.data.remote.dto.VCRequestDto
import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VersionControlRepositoryImpl(
    private val remoteDataSource: VersionControlRemoteDataSource,
    private val apkDownloader: ApkDownloader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : VersionControlRepository {

    override suspend fun checkVersion(config: VersionControlConfig): Result<UpdateStatus> {
        return withContext(ioDispatcher) {
            runCatching {
                val inputDto = config.toCheckVersionInputDto()
                val responseDto = remoteDataSource.checkVersion(config.baseUrl, VCRequestDto(inputDto))
                val statusCode = responseDto.status?.code
                if (statusCode.isResponseSuccessful()) {
                    responseDto.toUIModel()
                } else {
                    throw IllegalStateException(responseDto.status?.description ?: "Server error: $statusCode")
                }
            }
        }
    }

    override suspend fun getLastVersion(config: VersionControlConfig): Result<UpdateInfo> {
        return withContext(ioDispatcher) {
            runCatching {
                val inputDto = config.toGetLastVersionInputDto()
                val responseDto = remoteDataSource.getLastVersion(config.baseUrl, VCRequestDto(inputDto))
                val statusCode = responseDto.status?.code
                if (statusCode.isResponseSuccessful()) {
                    responseDto.toUIModel()
                } else {
                    throw IllegalStateException(responseDto.status?.description ?: "Server error: $statusCode")
                }
            }
        }
    }

    override fun downloadApk(url: String, destinationPath: String): Flow<DownloadState> {
        return apkDownloader.downloadApk(url, destinationPath)
    }
}
