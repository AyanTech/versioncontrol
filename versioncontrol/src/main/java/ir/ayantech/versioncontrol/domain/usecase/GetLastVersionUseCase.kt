package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.model.UpdateInfo

interface GetLastVersionUseCase {
    suspend operator fun invoke(config: VersionControlConfig): Result<UpdateInfo>
}
