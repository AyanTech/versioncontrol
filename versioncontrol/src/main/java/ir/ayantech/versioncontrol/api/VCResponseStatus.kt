package ir.ayantech.versioncontrol.api

import ir.ayantech.versioncontrol.model.VCResponseModel

interface VCResponseStatus {
    fun onSuccess(
        versionControlAPI: VersionControlAPI<*, *>?,
        message: String?,
        responseModel: VCResponseModel?
    )

    fun onFail(
        versionControlAPI: VersionControlAPI<*, *>?,
        error: String?,
        canTry: Boolean
    )
}
