package ir.ayantech.versioncontrol.domain.usecase.impl

import ir.ayantech.versioncontrol.data.mapper.getExceptionOrNull
import ir.ayantech.versioncontrol.data.mapper.isResponseSuccessful
import ir.ayantech.versioncontrol.data.mapper.toColocationEndpoints
import ir.ayantech.versioncontrol.domain.model.ColocationConfigResult
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.GetApplicationColocationConfigUseCase

class GetApplicationColocationConfigUseCaseImpl(
    private val repository: VersionControlRepository,
) : GetApplicationColocationConfigUseCase {

    override suspend operator fun invoke(
        applicationName: String,
        applicationType: String,
        applicationVersion: String,
        iranBaseUrl: String,
        internationalBaseUrl: String,
    ): Result<ColocationConfigResult> {
        return repository.getApplicationColocationConfig(
            applicationName = applicationName,
            applicationType = applicationType,
            applicationVersion = applicationVersion,
            iranBaseUrl = iranBaseUrl,
            internationalBaseUrl = internationalBaseUrl,
        ).mapCatching { responseDto ->
            if (!responseDto.status.isResponseSuccessful()) {
                throw (responseDto.status.getExceptionOrNull()
                    ?: IllegalStateException("Server error: ${responseDto.status?.code}"))
            }

            val endpoints = responseDto.toColocationEndpoints()
            if (endpoints.isEmpty()) {
                throw IllegalStateException("Empty endpoint list")
            }

            val versionControlUrl = endpoints.firstOrNull {
                it.name.equals("VersionControl", ignoreCase = true)
            }?.url

            ColocationConfigResult(
                endpointList = endpoints,
                versionControlBaseUrl = versionControlUrl,
            )
        }
    }
}
