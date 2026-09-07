package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository

class ShareAppUseCase(
    private val repository: VersionControlRepository
) {
    suspend operator fun invoke(config: VersionControlConfig): Result<String> {
        return repository.getLastVersion(config).mapCatching { updateInfo ->
            updateInfo.textToShare ?: throw IllegalStateException("No text available to share")
        }
    }
}
