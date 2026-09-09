package ir.ayantech.versioncontrol.domain.usecase.impl

import ir.ayantech.versioncontrol.VersionControlConfig
import ir.ayantech.versioncontrol.data.mapper.getExceptionOrNull
import ir.ayantech.versioncontrol.data.mapper.isResponseSuccessful
import ir.ayantech.versioncontrol.data.mapper.toGetLastVersionInputDto
import ir.ayantech.versioncontrol.data.mapper.toUIModel
import ir.ayantech.versioncontrol.domain.model.UpdateInfo
import ir.ayantech.versioncontrol.domain.repository.VersionControlRepository
import ir.ayantech.versioncontrol.domain.usecase.GetLastVersionUseCase

class GetLastVersionUseCaseImpl(
    private val repository: VersionControlRepository,
) : GetLastVersionUseCase {

    override suspend operator fun invoke(config: VersionControlConfig): Result<UpdateInfo> {
        val inputDto = config.toGetLastVersionInputDto()
        return repository.getLastVersion(config.baseUrl, inputDto).mapCatching { responseDto ->
            if (responseDto.status.isResponseSuccessful()) {
                responseDto.toUIModel()
            } else {
                throw (responseDto.status.getExceptionOrNull()
                    ?: IllegalStateException("Server error: ${responseDto.status?.code}"))
            }
        }
    }
}
