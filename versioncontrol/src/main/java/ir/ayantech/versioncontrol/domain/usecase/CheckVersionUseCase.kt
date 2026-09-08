package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.model.VersionCheckResult

interface CheckVersionUseCase {
    suspend operator fun invoke(config: VersionControlConfig): VersionCheckResult
}
