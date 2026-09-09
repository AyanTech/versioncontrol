package ir.ayantech.versioncontrol.domain.usecase.impl

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.mapper.getExceptionOrNull
import ir.ayantech.versioncontrol.data.mapper.isResponseSuccessful
import ir.ayantech.versioncontrol.data.mapper.toCheckVersionInputDto
import ir.ayantech.versioncontrol.data.mapper.toUIModel
import ir.ayantech.versioncontrol.domain.model.UpdateStatus
import ir.ayantech.versioncontrol.domain.model.VersionCheckResult
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.CheckVersionUseCase
import ir.ayantech.versioncontrol.domain.usecase.GetLastVersionUseCase

class CheckVersionUseCaseImpl(
    private val repository: VersionControlRepository,
    private val getLastVersionUseCase: GetLastVersionUseCase = GetLastVersionUseCaseImpl(repository),
) : CheckVersionUseCase {

    override suspend operator fun invoke(config: VersionControlConfig): VersionCheckResult {
        val inputDto = config.toCheckVersionInputDto()
        val checkResult = repository.checkVersion(config.baseUrl, inputDto)

        return checkResult.fold(
            onSuccess = { responseDto ->
                if (!responseDto.status.isResponseSuccessful()) {
                    val exception = responseDto.status.getExceptionOrNull()
                        ?: IllegalStateException("Server error: ${responseDto.status?.code}")
                    return VersionCheckResult.Failure(
                        message = exception.localizedMessage ?: "Failed to check version",
                        cause = exception,
                    )
                }

                val status = responseDto.toUIModel()
                if (status == UpdateStatus.NOT_REQUIRED) {
                    VersionCheckResult.UpToDate
                } else {
                    getLastVersionUseCase(config).fold(
                        onSuccess = { updateInfo ->
                            VersionCheckResult.UpdateAvailable(updateInfo.copy(updateStatus = status))
                        },
                    ) { throwable ->
                        VersionCheckResult.Failure(
                            message = throwable.localizedMessage ?: "Failed to get version details",
                            cause = throwable,
                        )
                    }
                }
            },
            onFailure = { throwable ->
                VersionCheckResult.Failure(
                    message = throwable.localizedMessage ?: "Failed to check version",
                    cause = throwable,
                )
            },
        )
    }
}
