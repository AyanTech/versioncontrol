package ir.ayantech.versioncontrol.domain.repository

import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetApplicationColocationConfigResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface VersionControlRepository {
    suspend fun checkVersion(
        baseUrl: String,
        inputDto: CheckVersionInputDto,
    ): Result<CheckVersionResponseDto>

    suspend fun getLastVersion(
        baseUrl: String,
        inputDto: GetLastVersionInputDto,
    ): Result<GetLastVersionResponseDto>

    fun downloadApk(url: String, destinationPath: String): Flow<DownloadState>

    suspend fun getApplicationColocationConfig(
        applicationName: String,
        applicationType: String,
        applicationVersion: String,
        iranBaseUrl: String,
        internationalBaseUrl: String,
    ): Result<GetApplicationColocationConfigResponseDto>
}
