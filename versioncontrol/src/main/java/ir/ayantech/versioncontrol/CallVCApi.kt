package ir.ayantech.versioncontrol

import ir.ayantech.versioncontrol.api.VCResponseStatus
import ir.ayantech.versioncontrol.model.VCInputModel

interface CallVCApi<RequestModel : VCInputModel> {
    fun callApi(status: VCResponseStatus, inputModel: RequestModel)
}
