package ir.ayantech.versioncontrol.domain.usecase

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import ir.ayantech.versioncontrol.domain.model.VersionCheckResult
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository

class CheckVersionUseCase(
    private val repository: VersionControlRepository
) {
    suspend operator fun invoke(config: VersionControlConfig): VersionCheckResult {
        return repository.checkVersion(config).fold(
            onSuccess = { status ->
                if (status == UpdateStatus.NOT_REQUIRED) {
                    VersionCheckResult.UpToDate
                } else {
                    repository.getLastVersion(config).fold(
                        onSuccess = { updateInfo ->
                            VersionCheckResult.UpdateAvailable(updateInfo.copy(updateStatus = status))
                        },
                        onFailure = { throwable ->
                            VersionCheckResult.Failure(
                                message = throwable.localizedMessage ?: "Failed to get version details",
                                cause = throwable
                            )
                        }
                    )
                }
            },
            onFailure = { throwable ->
                VersionCheckResult.Failure(
                    message = throwable.localizedMessage ?: "Failed to check version",
                    cause = throwable
                )
            }
        )
    }
}
