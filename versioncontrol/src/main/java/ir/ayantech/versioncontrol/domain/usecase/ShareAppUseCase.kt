package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.VersionControlConfig

interface ShareAppUseCase {
    suspend operator fun invoke(config: VersionControlConfig): Result<String>
}
