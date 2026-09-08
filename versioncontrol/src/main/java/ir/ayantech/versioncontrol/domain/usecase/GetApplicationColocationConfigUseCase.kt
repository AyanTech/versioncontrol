package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.model.ColocationConfigResult

interface GetApplicationColocationConfigUseCase {
    suspend operator fun invoke(
        applicationName: String,
        applicationType: String = VersionControlConfig.DEFAULT_APPLICATION_TYPE,
        applicationVersion: String,
        iranBaseUrl: String,
        internationalBaseUrl: String,
    ): Result<ColocationConfigResult>
}
