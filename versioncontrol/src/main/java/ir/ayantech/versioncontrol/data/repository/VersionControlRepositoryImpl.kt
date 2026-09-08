package ir.ayantech.versioncontrol.data.repository

import ir.ayantech.versioncontrol.data.downloader.ApkDownloader
import ir.ayantech.versioncontrol.data.mapper.getExceptionOrNull
import ir.ayantech.versioncontrol.data.mapper.isResponseSuccessful
import ir.ayantech.versioncontrol.data.mapper.toColocationEndpoints
import ir.ayantech.versioncontrol.data.remote.VersionControlRemoteDataSource
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigInputDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCRequestDto
import ir.ayantech.versioncontrol.domain.model.ColocationLane
import ir.ayantech.versioncontrol.domain.model.DownloadState
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VersionControlRepositoryImpl(
    private val remoteDataSource: VersionControlRemoteDataSource,
    private val apkDownloader: ApkDownloader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VersionControlRepository {

    override suspend fun checkVersion(
        baseUrl: String, inputDto: CheckVersionInputDto
    ): Result<CheckVersionResponseDto> {
        return withContext(ioDispatcher) {
            runCatching {
                remoteDataSource.checkVersion(baseUrl, VCRequestDto(inputDto))
            }
        }
    }

    override suspend fun getLastVersion(
        baseUrl: String, inputDto: GetLastVersionInputDto
    ): Result<GetLastVersionResponseDto> {
        return withContext(ioDispatcher) {
            runCatching {
                remoteDataSource.getLastVersion(baseUrl, VCRequestDto(inputDto))
            }
        }
    }

    override fun downloadApk(url: String, destinationPath: String): Flow<DownloadState> {
        return apkDownloader.downloadApk(url, destinationPath)
    }

    override suspend fun getApplicationColocationConfig(
        applicationName: String,
        applicationType: String,
        applicationVersion: String,
        iranBaseUrl: String,
        internationalBaseUrl: String,
    ): Result<GetApplicationColocationConfigResponseDto> = withContext(ioDispatcher) {
        if (iranBaseUrl.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Iran base URL must be provided by the application.")
            )
        }
        if (internationalBaseUrl.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("International base URL must be provided by the application.")
            )
        }

        val iranResult = requestColocationConfig(
            applicationName = applicationName,
            applicationType = applicationType,
            applicationVersion = applicationVersion,
            lane = ColocationLane.IRAN,
            baseUrl = iranBaseUrl
        )
        val iranResponse = iranResult.getOrNull()
        if (iranResponse?.status?.isResponseSuccessful() == true) {
            return@withContext Result.success(iranResponse)
        }

        val internationalResult = requestColocationConfig(
            applicationName = applicationName,
            applicationType = applicationType,
            applicationVersion = applicationVersion,
            lane = ColocationLane.INTERNATIONAL,
            baseUrl = internationalBaseUrl
        )
        return@withContext internationalResult
    }

    private suspend fun requestColocationConfig(
        applicationName: String,
        applicationType: String,
        applicationVersion: String,
        lane: ColocationLane,
        baseUrl: String,
    ): Result<GetApplicationColocationConfigResponseDto> {
        return try {
            val inputDto = GetApplicationColocationConfigInputDto(
                applicationType = applicationType,
                applicationName = applicationName,
                colocationType = lane.laneId,
                currentApplicationVersion = applicationVersion
            )
            val response = remoteDataSource.getApplicationColocationConfig(
                baseUrl = baseUrl, request = VCRequestDto(inputDto)
            )
            when {
                !response.status.isResponseSuccessful() -> Result.failure(
                    response.status.getExceptionOrNull()
                        ?: IllegalStateException("Server error: ${response.status?.code}")
                )

                response.toColocationEndpoints().isEmpty() -> Result.failure(
                    IllegalStateException("Empty endpoint list from lane ${lane.laneId}")
                )

                else -> Result.success(response)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
