package ir.ayantech.versioncontrol.data.remote

import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCRequestDto

interface VersionControlRemoteDataSource {
    suspend fun checkVersion(
        baseUrl: String,
        request: VCRequestDto<CheckVersionInputDto>
    ): CheckVersionResponseDto

    suspend fun getLastVersion(
        baseUrl: String,
        request: VCRequestDto<GetLastVersionInputDto>
    ): GetLastVersionResponseDto
}
