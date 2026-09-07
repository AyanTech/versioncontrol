package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository

class GetLastVersionUseCase(
    private val repository: VersionControlRepository
) {
    suspend operator fun invoke(config: VersionControlConfig): Result<UpdateInfo> {
        return repository.getLastVersion(config)
    }
}
