package ir.ayantech.versioncontrol.domain.usecase.impl

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.GetLastVersionUseCase
import ir.ayantech.versioncontrol.domain.usecase.ShareAppUseCase

class ShareAppUseCaseImpl(
    private val getLastVersionUseCase: GetLastVersionUseCase,
) : ShareAppUseCase {

    constructor(repository: VersionControlRepository) : this(GetLastVersionUseCaseImpl(repository))

    override suspend operator fun invoke(config: VersionControlConfig): Result<String> {
        return getLastVersionUseCase(config).mapCatching { updateInfo ->
            updateInfo.textToShare?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("No text available to share")
        }
    }
}
