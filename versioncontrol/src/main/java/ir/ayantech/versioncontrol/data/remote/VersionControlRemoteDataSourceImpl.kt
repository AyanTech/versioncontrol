package ir.ayantech.versioncontrol.data.remote

import ir.ayantech.versioncontrol.data.remote.client.VersionControlHttpClient
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.CheckVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionInputDto
import ir.ayantech.versioncontrol.data.remote.dto.GetLastVersionResponseDto
import ir.ayantech.versioncontrol.data.remote.dto.VCRequestDto

class VersionControlRemoteDataSourceImpl(
    private val httpClient: VersionControlHttpClient = VersionControlHttpClient()
) : VersionControlRemoteDataSource {

    override suspend fun checkVersion(
        baseUrl: String,
        request: VCRequestDto<CheckVersionInputDto>
    ): CheckVersionResponseDto {
        return httpClient.post(
            baseUrl = baseUrl,
            endpoint = "checkVersion",
            requestBody = request
        )
    }

    override suspend fun getLastVersion(
        baseUrl: String,
        request: VCRequestDto<GetLastVersionInputDto>
    ): GetLastVersionResponseDto {
        return httpClient.post(
            baseUrl = baseUrl,
            endpoint = "getLastVersion",
            requestBody = request
        )
    }
}
